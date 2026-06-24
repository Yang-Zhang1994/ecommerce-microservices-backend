#!/usr/bin/env bash
# 压测时实时看 JVM 堆：Eden/Old/Survivor 内存，Young GC / Old GC 次数与耗时
# 用法: ./scripts/watch-heap.sh [BASE_URL]
# 需 Micrometer 1.11+ 才有 gc=G1 Young/Old Generation 标签；否则 Young/Old GC 显示为 0

BASE="${1:-http://localhost:10000}"

# 取指标并转为整数（Actuator 可能返回浮点，bash 算术只接受整数）
metric() {
  curl -s "$BASE/actuator/metrics/$1${2:+?tag=$2}" | jq -r '(.measurements[0].value // 0) | floor'
}

# 从 jvm.gc.pause 的 JSON 中取 COUNT 和 TOTAL_TIME（秒）
gc_count_time() {
  local json
  json=$(curl -s "$BASE/actuator/metrics/jvm.gc.pause${1:+?tag=$1}")
  local c t
  c=$(echo "$json" | jq -r '([.measurements[]? | select(.statistic=="COUNT") | .value][0] // 0) | floor')
  t=$(echo "$json" | jq -r '[.measurements[]? | select(.statistic=="TOTAL_TIME") | .value][0] // 0')
  echo "$c $t"
}

echo "Polling $BASE every 2s (Ctrl+C to stop)"
echo "---"

while true; do
  eden=$(metric "jvm.memory.used" "id:G1%20Eden%20Space")
  old=$(metric "jvm.memory.used" "id:G1%20Old%20Gen")
  survivor=$(metric "jvm.memory.used" "id:G1%20Survivor%20Space")
  allocated=$(metric "jvm.gc.memory.allocated")
  promoted=$(metric "jvm.gc.memory.promoted")
  allocated=${allocated:-0}
  promoted=${promoted:-0}

  # Young GC：优先用 gc=G1 Young Generation（Micrometer 1.11+），否则用 action=end of minor GC
  yg_raw=$(gc_count_time "gc:G1%20Young%20Generation")
  yg_count=${yg_raw%% *}
  yg_time=${yg_raw#* }
  if [[ "${yg_count:-0}" -eq 0 ]]; then
    yg_raw=$(gc_count_time "action:end%20of%20minor%20GC")
    yg_count=${yg_raw%% *}
    yg_time=${yg_raw#* }
  fi
  yg_time_ms=$(echo "$yg_time * 1000" | bc 2>/dev/null || echo "0")

  # Old GC：优先用 gc=G1 Old Generation，否则用 action=end of major GC
  og_raw=$(gc_count_time "gc:G1%20Old%20Generation")
  og_count=${og_raw%% *}
  og_time=${og_raw#* }
  if [[ "${og_count:-0}" -eq 0 ]]; then
    og_raw=$(gc_count_time "action:end%20of%20major%20GC")
    og_count=${og_raw%% *}
    og_time=${og_raw#* }
  fi
  og_time_ms=$(echo "$og_time * 1000" | bc 2>/dev/null || echo "0")

  # 若 Young/Old 仍都为 0，用合计（action:end）至少显示总 GC
  total_gc_raw=""
  if [[ "${yg_count:-0}" -eq 0 && "${og_count:-0}" -eq 0 ]]; then
    total_gc_raw=$(gc_count_time "action:end")
  fi

  # 去掉可能的小数部分，避免 bash 算术报错
  eden=${eden%.*}; old=${old%.*}; survivor=${survivor%.*}
  allocated=${allocated%.*}; promoted=${promoted%.*}

  eden_mb=$((eden / 1024 / 1024))
  old_mb=$((old / 1024 / 1024))
  survivor_mb=$((survivor / 1024 / 1024))
  allocated_mb=$((allocated / 1024 / 1024))
  promoted_mb=$((promoted / 1024 / 1024))

  printf "%s  Eden: %4d MB | Old: %4d MB | Surv: %3d MB\n" \
    "$(date +%H:%M:%S)" "$eden_mb" "$old_mb" "$survivor_mb"
  if [[ -n "$total_gc_raw" ]]; then
    total_c=${total_gc_raw%% *}
    total_t=${total_gc_raw#* }
    total_t_ms=$(echo "$total_t * 1000" | bc 2>/dev/null || echo "0")
    printf "     GC (合计): %s 次, %s ms  (无 Young/Old 标签时显示)  |  分配: %d MB  晋升: %d MB\n" \
      "$total_c" "$total_t_ms" "$allocated_mb" "$promoted_mb"
  else
    printf "     Young GC: %s 次, %s ms  |  Old GC: %s 次, %s ms  |  分配: %d MB  晋升: %d MB\n" \
      "$yg_count" "$yg_time_ms" "$og_count" "$og_time_ms" "$allocated_mb" "$promoted_mb"
  fi
  sleep 2
done

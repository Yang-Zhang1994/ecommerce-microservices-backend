#!/bin/bash
# 在项目根目录执行，为 renren-fast 子模块应用验证码 500 修复（Quartz 排除 + 验证码内存回退）
# 用法: cd /path/to/gulimall && bash scripts/apply-captcha-fix.sh
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"
RENREN_FAST="$REPO_ROOT/renren-fast"
if [ ! -d "$RENREN_FAST" ]; then
  echo "Error: renren-fast not found at $RENREN_FAST"
  exit 1
fi
# 1) 在 application.yml 的 autoconfigure.exclude 中加入 QuartzAutoConfiguration（若尚未存在）
APPLICATION_YML="$RENREN_FAST/src/main/resources/application.yml"
if [ -f "$APPLICATION_YML" ]; then
  if ! grep -q "QuartzAutoConfiguration" "$APPLICATION_YML"; then
    # 在 ShiroAutoConfiguration 行后插入
    perl -i -pe 's/(ShiroAutoConfiguration)/$1\n      - org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration/ if !$done++' "$APPLICATION_YML"
    echo "Added QuartzAutoConfiguration exclude to application.yml"
  else
    echo "application.yml already contains Quartz exclude, skip"
  fi
fi
# 2) 覆盖 SysCaptchaServiceImpl.java 为带内存回退的版本
SRC_JAVA="$SCRIPT_DIR/renren-fast-fix/SysCaptchaServiceImpl.java"
DEST_JAVA="$RENREN_FAST/src/main/java/io/renren/modules/sys/service/impl/SysCaptchaServiceImpl.java"
if [ -f "$SRC_JAVA" ]; then
  mkdir -p "$(dirname "$DEST_JAVA")"
  cp "$SRC_JAVA" "$DEST_JAVA"
  echo "Applied SysCaptchaServiceImpl.java (in-memory captcha fallback)"
else
  echo "Warning: $SRC_JAVA not found, skip Java fix"
fi
# 3) 覆盖 ScheduleConfig.java：Quartz 不自动启动，避免 RDS 不可达时阻塞启动
SCHEDULE_SRC="$SCRIPT_DIR/renren-fast-fix/ScheduleConfig.java"
SCHEDULE_DEST="$RENREN_FAST/src/main/java/io/renren/modules/job/config/ScheduleConfig.java"
if [ -f "$SCHEDULE_SRC" ]; then
  mkdir -p "$(dirname "$SCHEDULE_DEST")"
  cp "$SCHEDULE_SRC" "$SCHEDULE_DEST"
  echo "Applied ScheduleConfig.java (Quartz auto-startup disabled)"
fi
# 4) Druid Wall：允许 JPA 生成的 select-always-true 条件，避免 admin list 500
for YML in "$RENREN_FAST/src/main/resources/application-dev.yml" "$RENREN_FAST/src/main/resources/application-prod.yml"; do
  if [ -f "$YML" ] && ! grep -q "select-always-true-allow" "$YML"; then
    perl -i -pe 's/(multi-statement-allow: true)/$1\n            select-always-true-allow: true/' "$YML"
    echo "Applied Druid Wall select-always-true-allow to $(basename "$YML")"
  fi
done
echo "Captcha fix applied. Rebuild renren-fast and restart: docker compose -f docker-compose.app.yml build renren-fast && docker compose -f docker-compose.app.yml up -d renren-fast"

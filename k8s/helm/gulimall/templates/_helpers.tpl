{{- define "gulimall.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "gulimall.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "gulimall.image" -}}
{{- $registry := trimSuffix "/" .Values.global.imageRegistry -}}
{{- $repo := .repository -}}
{{- $tag := .tag | default "latest" -}}
{{- if .Values.global.imageTag -}}
{{- $tag = .Values.global.imageTag -}}
{{- end -}}
{{- if $registry -}}
{{- printf "%s/%s:%s" $registry $repo $tag }}
{{- else -}}
{{- printf "%s:%s" $repo $tag }}
{{- end }}
{{- end }}

{{- define "gulimall.labels" -}}
app.kubernetes.io/name: {{ include "gulimall.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ include "gulimall.name" . }}-{{ .Chart.Version }}
{{- end }}

{{- define "gulimall.consulEnv" -}}
- name: SPRING_CLOUD_CONSUL_HOST
  value: consul
- name: SPRING_CLOUD_CONSUL_PORT
  value: "8500"
{{- end }}

{{- define "gulimall.redisEnv" -}}
- name: SPRING_DATA_REDIS_HOST
  value: redis
- name: SPRING_DATA_REDIS_PORT
  value: "6379"
{{- end }}

{{- define "gulimall.rabbitEnv" -}}
- name: SPRING_RABBITMQ_HOST
  value: rabbitmq
- name: SPRING_RABBITMQ_PORT
  value: "5672"
- name: SPRING_RABBITMQ_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Values.secrets.existingSecret }}
      key: rabbitmq-username
- name: SPRING_RABBITMQ_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.secrets.existingSecret }}
      key: rabbitmq-password
{{- end }}

{{- define "gulimall.rdsEnv" -}}
- name: SPRING_DATASOURCE_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Values.secrets.existingSecret }}
      key: rds-username
- name: SPRING_DATASOURCE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.secrets.existingSecret }}
      key: rds-password
{{- end }}

{{- define "gulimall.otelEnv" -}}
{{- if .Values.otel.enabled }}
- name: OTEL_TRACES_EXPORTER
  value: otlp
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: {{ .Values.otel.exporterEndpoint | quote }}
- name: OTEL_EXPORTER_OTLP_PROTOCOL
  value: {{ .Values.otel.exporterProtocol | quote }}
- name: OTEL_INSTRUMENTATION_MESSAGING_EXPERIMENTAL_RECEIVE_TELEMETRY_ENABLED
  value: {{ .Values.otel.messagingReceiveTelemetry | quote }}
- name: OTEL_TRACES_SAMPLER
  value: {{ .Values.otel.tracesSampler | quote }}
- name: JAVA_TOOL_OPTIONS
  value: -javaagent:/otel/opentelemetry-javaagent.jar
{{- end }}
{{- end }}

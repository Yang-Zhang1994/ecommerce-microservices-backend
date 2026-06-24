FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /otel
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.12.0/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY target/gulimall-order-*.jar app.jar
EXPOSE 9000
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

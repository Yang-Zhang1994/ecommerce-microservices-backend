FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gulimall-review-*.jar app.jar
EXPOSE 15000
ENV JAVA_OPTS=""
# Unprivileged by default; the chart also sets runAsNonRoot (platform/policy checks both).
USER 1000:1000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

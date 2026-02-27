FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gulimall-third-party-*.jar app.jar
EXPOSE 30000
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

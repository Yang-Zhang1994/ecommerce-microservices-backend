FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gulimall-auth-server-*.jar app.jar
EXPOSE 20000
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

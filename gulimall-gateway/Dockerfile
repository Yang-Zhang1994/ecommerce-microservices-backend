FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gulimall-gateway-*.jar app.jar
EXPOSE 88
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

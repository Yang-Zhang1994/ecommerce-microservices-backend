FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gulimall-seckill-*.jar app.jar
EXPOSE 14000
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

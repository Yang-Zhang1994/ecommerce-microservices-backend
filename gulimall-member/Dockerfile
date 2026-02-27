FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gulimall-member-*.jar app.jar
EXPOSE 8000
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

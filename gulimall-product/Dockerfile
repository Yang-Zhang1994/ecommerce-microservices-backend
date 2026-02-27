FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gulimall-product-*.jar app.jar
EXPOSE 10000
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

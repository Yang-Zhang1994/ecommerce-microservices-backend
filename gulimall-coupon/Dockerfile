FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gulimall-coupon-*.jar app.jar
EXPOSE 12000
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/gulimall-search-*.jar app.jar

EXPOSE 12001

ENTRYPOINT ["java","-jar","/app/app.jar"]

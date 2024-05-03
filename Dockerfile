FROM openjdk:19

WORKDIR /app

COPY target/scala-2.13/library_api-assembly-0.1.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

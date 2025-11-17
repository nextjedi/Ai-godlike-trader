FROM mcr.microsoft.com/playwright/java:v1.39.0-jammy
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 80
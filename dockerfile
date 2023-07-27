FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} *.jar
ENTRYPOINT ["java","${JAVA_OPTS}","-jar","/app.jar"]
FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
WORKDIR /app
COPY ${JAR_FILE} /app/econtract.jar

RUN mkdir -p /app/uploads && chmod -R 777 /app/uploads

ENTRYPOINT ["java","-jar","/app/econtract.jar"]

FROM maven:3.6.3-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM openjdk:11.0.4-jre-slim
COPY --from=build /app/target/kafka-kotlin-example*-jar-with-dependencies.jar /app/kafka-tutorial.jar
ENTRYPOINT ["java", "-jar", "/app/kafka-tutorial.jar"]
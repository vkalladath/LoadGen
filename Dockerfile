# Build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/LoadGen/src
COPY pom.xml /home/LoadGen
RUN mvn -f /home/LoadGen/pom.xml clean package

# Package stage
FROM openjdk:11-jre-slim
COPY --from=build /home/LoadGen/target/LoadGen-1.0.jar /usr/local/lib/LoadGen.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/LoadGen.jar"]

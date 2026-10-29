FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
COPY index.html .
COPY about about
COPY access access
COPY admin admin
COPY css css
COPY data data
COPY docs docs
COPY faq faq
COPY img img
COPY information information
COPY js js
COPY medical medical
COPY reservation reservation

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

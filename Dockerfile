FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -Dmaven.test.skip=true dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S taskflow && adduser -S taskflow -G taskflow

COPY --from=build /workspace/target/*.jar app.jar

USER taskflow

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

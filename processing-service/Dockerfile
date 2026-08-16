FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY shared-model/pom.xml shared-model/pom.xml
COPY ingestion-service/pom.xml ingestion-service/pom.xml
COPY processing-service/pom.xml processing-service/pom.xml
COPY shared-model/src shared-model/src
COPY processing-service/src processing-service/src

RUN mvn -q -pl processing-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=build /workspace/processing-service/target/processing-service-*.jar /app/app.jar

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

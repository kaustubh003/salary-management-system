FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
COPY scripts ./scripts
RUN mvn -B package dependency:copy-dependencies \
    -DincludeArtifactIds=h2 \
    -DoutputDirectory=/workspace/dependencies

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app app \
    && mkdir -p /app/data /app/docs \
    && chown -R app:app /app

COPY --from=build --chown=app:app /workspace/target/salary-0.0.1-SNAPSHOT.jar /app/app.jar
COPY --from=build --chown=app:app /workspace/dependencies/h2-*.jar /app/h2.jar
COPY --from=build --chown=app:app /workspace/scripts /app/scripts

VOLUME ["/app/data", "/app/docs"]
EXPOSE 8080
USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

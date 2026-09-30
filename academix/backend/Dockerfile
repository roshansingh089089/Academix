FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system academix \
    && useradd --system --gid academix --home-dir /app --shell /usr/sbin/nologin academix \
    && mkdir -p /data \
    && chown -R academix:academix /app /data

COPY --from=build --chown=academix:academix /workspace/build/libs/academix-backend-0.1.0.jar /app/academix.jar

ENV SPRING_PROFILES_ACTIVE=production \
    ACADEMIX_DB_PATH=/data/academix.db

USER academix

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/academix.jar"]

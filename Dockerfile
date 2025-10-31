# ==== BUILD ====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

# ==== RUN ====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/backend-mini-app-0.0.1-SNAPSHOT.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"
EXPOSE 8080

CMD sh -c 'P="${PORT:-8080}"; case "$P" in (*[!0-9]*) P=8080;; esac; \
  echo "Using server.port=$P"; \
  unset PORT HTTP_PROXY HTTPS_PROXY NO_PROXY; \
  exec java -Dserver.port="$P" -jar /app/app.jar'
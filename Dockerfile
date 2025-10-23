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

# Giữ JVM gọn cho máy nhỏ; **bỏ -XshowSettings:vm**
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

# Render cấp PORT động; 8080 để tài liệu/local
EXPOSE 8080

# Dùng shell để expand $PORT và truyền cho Spring
CMD sh -c 'java -Dserver.port=${PORT:-8080} -jar /app/app.jar'

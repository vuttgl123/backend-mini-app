# Giai đoạn build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy toàn bộ source code
COPY pom.xml .
COPY src ./src

# Build file jar
RUN mvn clean package -DskipTests

# Giai đoạn run
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy file jar từ giai đoạn build
COPY --from=build /app/target/backend-mini-app-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (Render sẽ dùng biến PORT)
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]

# Sử dụng JDK chính thức
FROM eclipse-temurin:21-jdk

# Copy file jar đã build vào container
COPY target/backend-mini-app-0.0.1-SNAPSHOT.jar app.jar

# Cấu hình cổng (Render sẽ dùng PORT env var)
EXPOSE 8080

# Lệnh khởi chạy app
ENTRYPOINT ["java","-jar","/app.jar"]

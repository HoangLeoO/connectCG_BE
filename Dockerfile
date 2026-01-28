# --- Giai đoạn 1: Build ứng dụng ---
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy các file cấu hình gradle trước để tận dụng cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Cấp quyền thực thi cho gradlew
RUN chmod +x gradlew

# Copy toàn bộ source code
COPY src src

# Thực hiện build (bỏ qua test để tiết kiệm thời gian)
RUN ./gradlew clean bootJar -x test

# --- Giai đoạn 2: Chạy ứng dụng ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Chỉ copy file .jar từ giai đoạn builder sang
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (Render thường dùng biến môi trường PORT, nhưng mặc định spring boot là 8080)
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
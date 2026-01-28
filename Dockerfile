# --- Giai đoạn 1: Build ứng dụng ---
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy các file cấu hình gradle trước để tận dụng cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Chuyển về Gradle 8.7 bên trong Docker để build ổn định (không ảnh hưởng file gốc)
RUN sed -i 's/gradle-9.2.1-bin.zip/gradle-8.7-bin.zip/g' gradle/wrapper/gradle-wrapper.properties

# Cấp quyền thực thi cho gradlew
RUN chmod +x gradlew

# Copy toàn bộ source code
COPY src src

# Thực hiện build (bỏ qua test để tiết kiệm thời gian)
RUN ./gradlew clean bootJar -x test

# --- Giai đoạn 2: Chạy ứng dụng ---
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Chỉ copy file .jar từ giai đoạn builder sang
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (Render thường dùng biến môi trường PORT, nhưng mặc định spring boot là 8080)
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
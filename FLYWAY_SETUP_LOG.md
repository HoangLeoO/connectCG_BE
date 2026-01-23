# NHẬT KÝ CÀI ĐẶT & MIGRATION FLYWAY

## 1. Cấu hình Dependencies (build.gradle)
- Đã thêm thư viện Flyway:
  ```groovy
  implementation 'org.flywaydb:flyway-core'
  implementation 'org.flywaydb:flyway-mysql'
  ```
- Đã thêm Flyway plugin (tùy chọn, dùng để chạy lệnh thủ công nếu cần):
  ```groovy
  id 'org.flywaydb.flyway' version '10.0.0'
  ```

## 2. Cấu hình Ứng dụng (application.properties)
- Cập nhật cấu hình JPA và Flyway để xử lý việc khởi tạo database an toàn:
  ```properties
  # JPA Validation (Bật kiểm tra sau khi migration thành công)
  spring.jpa.hibernate.ddl-auto=validate
  spring.jpa.show-sql=true

  # Cấu hình Flyway
  spring.flyway.enabled=true
  spring.flyway.clean-disabled=false  # Cho phép chạy lệnh 'clean' khi cần xử lý lỗi
  # spring.flyway.baseline-on-migrate=true # (Đã dùng tạm thời, giờ có thể bỏ)
  ```

## 3. Script Migration Database (V1__init_db_connect.sql)
- **Di chuyển file**: Chuyển file gốc từ `src/main/resources/V1_init_db_connect.sql` vào thư mục chuẩn `src/main/resources/db/migration/V1__init_db_connect.sql`.
- **Làm sạch SQL**:
  - Đã xóa/comment các lệnh `CREATE DATABASE` và `USE` trong file script. Flyway sẽ tự động kết nối vào đúng database được cấu hình, việc để các lệnh này lại sẽ gây lỗi quyền hạn hoặc lỗi kết nối.

## 4. Sửa Lỗi Entity (Khớp kiểu dữ liệu Java & MySQL)
Khắc phục lỗi `SchemaManagementException` do sự không khớp giữa Hibernate và Database MySQL đối với kiểu dữ liệu văn bản dài.
- **Vấn đề**: Hibernate định nghĩa `@Lob` thường map sang `TINYTEXT` hoặc `CLOB`, trong khi MySQL đang dùng `TEXT` (do Flyway tạo).
- **Giải pháp**: Xóa `@Lob` và khai báo rõ `columnDefinition = "TEXT"`.

### Các File Đã Sửa:
1.  **Comment.java**: Trường `content`
    ```java
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    ```
2.  **Post.java**: Trường `content`
    ```java
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    ```
3.  **Group.java**: Trường `description`
    ```java
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    ```
4.  **RefreshToken.java**: Trường `userAgent`
    ```java
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    ```
5.  **Report.java**: Trường `adminNote`
    ```java
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
    ```

## 5. Các bước xử lý sự cố đã thực hiện
- Chạy `gradlew clean` để xóa cache cũ.
- Ép buộc Flyway baseline/clean khi trạng thái database không đồng nhất.
- Sử dụng tạm `ManualFlywayConfig` để kích hoạt migration thủ công khi cấu hình tự động gặp trục trặc, sau đó đã xóa đi khi hệ thống ổn định.

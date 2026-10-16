# Jenkins CI/CD và Render

Luồng triển khai:

```text
GitHub -> Jenkins checkout -> Maven build -> Render Deploy Hook
        -> Render build Docker image and run app -> public Render URL
```

Jenkins kiểm tra code bằng Maven trong thư mục `lab1`. Khi build thành công, Jenkins gọi Render Deploy Hook; Render lấy commit mới nhất, build theo Dockerfile ở thư mục gốc và chạy ứng dụng. Render cung cấp URL công khai cho web service. Docker Compose không cần thiết cho luồng triển khai này.

## Thiết lập Render

1. Kết nối repository GitHub với Render và tạo Blueprint từ `render.yaml` ở thư mục gốc repository.
2. `render.yaml` khai báo runtime Docker bằng Dockerfile ở thư mục gốc và PostgreSQL.
3. Trong Render, mở Web Service → **Settings → Deploy Hook**, tạo hook và sao chép URL. Không đưa URL này vào Git.
4. Sau khi deploy thành công, lấy public URL của Web Service trong Render Dashboard.

## Thiết lập Jenkins

1. Trong **Manage Jenkins → Credentials**, thêm credential loại **Secret text**:
   - **Secret**: Render Deploy Hook URL
   - **ID**: `render-deploy-hook`
2. Tạo job **Pipeline**, chọn **Pipeline script from SCM**.
3. Cấu hình SCM là Git, điền URL repository và branch cần deploy.
4. Đặt **Script Path** là `Jenkinsfile` (repository root), lưu và chọn **Build Now**.
5. `Jenkinsfile` checkout code, chạy Maven Wrapper, sau đó gọi Deploy Hook chỉ khi build thành công. SCM polling kiểm tra thay đổi mỗi 5 phút.

Máy đang chạy Jenkins cần hoạt động để SCM polling chạy. Jenkins cần quyền đọc repository; nếu repository private, cấu hình Git credentials trong job.

## Biến môi trường và database

Render truyền `DATABASE_URL` từ PostgreSQL theo khai báo `fromDatabase` trong `render.yaml`. Không commit URL database hoặc Deploy Hook vào repository. Render thiết lập `PORT`; Docker entrypoint dùng cổng đó (mặc định 10000).

Ứng dụng dùng Hibernate `ddl-auto=update` để cập nhật schema khi khởi động. Nếu cần dữ liệu mẫu, nạp chúng riêng vào PostgreSQL; không dựa vào `initialQuery` trong Render Blueprint.

Flyway chạy migration trước khi Hibernate cập nhật schema. Với PostgreSQL hiện có, migration chuẩn hóa cột `books.cover_url` thành `TEXT` để URL ảnh dài không bị lỗi giới hạn 255 ký tự. Sao lưu database trước khi deploy bản migration đầu tiên.

### Tài khoản và phiên đăng nhập

Trong Render, mở Web Service → **Environment** và đặt:

- `APP_ADMIN_USERNAME`: tên tài khoản quản trị ban đầu.
- `APP_ADMIN_PASSWORD`: mật khẩu mạnh, tối thiểu 12 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.
- `SESSION_COOKIE_SECURE`: `true` (Blueprint đã bật cờ này cho HTTPS).

Không ghi mật khẩu vào Git, `render.yaml` hoặc Jenkinsfile. Nếu thiếu cả hai biến admin, ứng dụng vẫn chạy nhưng chưa có tài khoản quản trị; nếu chỉ đặt một biến, ứng dụng dừng lúc khởi động để tránh cấu hình nửa vời. Tài khoản admin được tạo/cập nhật từ hai biến khi ứng dụng khởi động. Đăng ký độc giả tạo role `USER`; không có màn hình đăng ký công khai tài khoản admin.

Khi chạy local qua HTTP, có thể để `SESSION_COOKIE_SECURE=false` (giá trị mặc định) để cookie phiên hoạt động. Render Blueprint bật `true` vì website chạy qua HTTPS.

## Kiểm tra khi lỗi

- **Jenkins Build lỗi**: mở **Console Output**; bước deploy sẽ không chạy.
- **Jenkins báo không có credential**: kiểm tra Jenkins credential ID chính xác là `render-deploy-hook`.
- **Render deploy lỗi**: kiểm tra Events/Logs của Web Service và trạng thái PostgreSQL trong Render Dashboard.
- **Không mở được URL**: đợi Render báo deploy thành công rồi dùng URL công khai của Web Service.

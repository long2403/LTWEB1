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

## Kiểm tra khi lỗi

- **Jenkins Build lỗi**: mở **Console Output**; bước deploy sẽ không chạy.
- **Jenkins báo không có credential**: kiểm tra Jenkins credential ID chính xác là `render-deploy-hook`.
- **Render deploy lỗi**: kiểm tra Events/Logs của Web Service và trạng thái PostgreSQL trong Render Dashboard.
- **Không mở được URL**: đợi Render báo deploy thành công rồi dùng URL công khai của Web Service.

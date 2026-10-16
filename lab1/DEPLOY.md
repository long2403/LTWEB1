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

Flyway chạy migration trước khi Hibernate cập nhật schema. Với PostgreSQL hiện có, migration chuẩn hóa cột `books.cover_url` thành `TEXT`, thêm metadata phân loại/đề cử cho truyện và cột đánh dấu bình luận đã xóa để giữ nguyên mạch phản hồi. Sao lưu database trước khi deploy migration mới.

### Tài khoản và phiên đăng nhập

Trong Render, mở Web Service → **Environment** và đặt:

- `APP_ADMIN_USERNAME`: tên tài khoản quản trị ban đầu.
- `APP_ADMIN_PASSWORD`: mật khẩu mạnh, tối thiểu 12 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.
- `SESSION_COOKIE_SECURE`: `true` (Blueprint đã bật cờ này cho HTTPS).

Không ghi mật khẩu vào Git, `render.yaml` hoặc Jenkinsfile. Nếu thiếu cả hai biến admin, ứng dụng vẫn chạy nhưng chưa có tài khoản quản trị; nếu chỉ đặt một biến, ứng dụng dừng lúc khởi động để tránh cấu hình nửa vời. Tài khoản admin được tạo/cập nhật từ hai biến khi ứng dụng khởi động. Đăng ký độc giả tạo role `USER`; không có màn hình đăng ký công khai tài khoản admin.

Khi chạy local qua HTTP, có thể để `SESSION_COOKIE_SECURE=false` (giá trị mặc định) để cookie phiên hoạt động. Render Blueprint bật `true` vì website chạy qua HTTPS.

## Tính năng độc giả

- Trang chủ chia truyện đề cử, truyện mới, truyện tranh và truyện chữ; admin chọn loại và bật đề cử trong form quản lý truyện. Danh sách chính dùng API phân trang theo trang, tìm kiếm, thể loại, trạng thái, loại truyện và sắp xếp.
- Trang chi tiết `/book.html?id=<id>` có danh sách chương, trình đọc theo loại truyện, bình luận và trả lời. Lượt xem tăng khi mở chi tiết.
- Người đăng nhập có thể sửa hoặc xóa bình luận/phản hồi của chính mình; quản trị viên có thể gỡ nội dung. Xóa bình luận thay nội dung bằng trạng thái đã xóa để không làm đứt các phản hồi bên dưới.
- `/library.html` lưu danh sách theo dõi, lịch sử đọc và thông báo. Tài khoản đăng nhập nhận thông báo khi có truyện mới, chương mới của truyện đang theo dõi hoặc người khác trả lời bình luận.
- Bìa không nhập URL sẽ tự sinh ảnh SVG trang trí theo tên truyện trên trang chủ, chi tiết, tủ sách và quản trị.
- `/games.html` có ba trò chơi nhỏ chạy ngay trong trình duyệt: lật thẻ tìm cặp, kéo búa bao và thử thách phản xạ.
- Biểu tượng nhóm chat trên trang chủ tải tin nhắn định kỳ; người đã đăng nhập có thể gửi tin nhắn, emoji và ảnh PNG/JPG/GIF/WebP tối đa 256 KB. Chat giữ tối đa 1.000 tin nhắn gần nhất trong PostgreSQL để giới hạn tăng dung lượng.
- Khi khởi động lần đầu sau bản này, ứng dụng tự thêm 30 truyện demo nguyên bản, mỗi truyện có 3 chương (90 chương tổng cộng) nếu chưa có; lần khởi động sau không tạo bản sao. Có thể xóa các truyện demo từ trang quản trị.

## Kiểm tra khi lỗi

- **Jenkins Build lỗi**: mở **Console Output**; bước deploy sẽ không chạy.
- **Jenkins báo không có credential**: kiểm tra Jenkins credential ID chính xác là `render-deploy-hook`.
- **Render deploy lỗi**: kiểm tra Events/Logs của Web Service và trạng thái PostgreSQL trong Render Dashboard.
- **Không mở được URL**: đợi Render báo deploy thành công rồi dùng URL công khai của Web Service.

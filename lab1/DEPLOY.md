# Deploy Docker lên AWS EC2

Hướng dẫn triển khai ứng dụng Spring Boot + PostgreSQL lên EC2 bằng Docker Compose nằm tại [DEPLOY.md](../DEPLOY.md). Cấu hình này dùng PostgreSQL nội bộ và không mở cổng database ra Internet.

`docker compose` đọc cấu hình nhạy cảm từ `.env`; không commit tệp này. Đặt `APP_ADMIN_USERNAME` và `APP_ADMIN_PASSWORD` cùng nhau để khởi tạo tài khoản quản trị. `SESSION_COOKIE_SECURE` phải là `false` khi truy cập HTTP và chỉ bật `true` sau khi cấu hình HTTPS.

Flyway chạy migration khi app khởi động. Sao lưu database trước khi deploy migration mới. PostgreSQL Compose giữ dữ liệu trong volume `postgres_data`.

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
- **Jenkins báo không có credential**: kiểm tra Jenkins credential IDs `ec2-deploy-key` và `ec2-host`.
- **Container app không chạy**: kiểm tra `docker compose logs --tail=200 app` trên EC2.
- **Không mở được URL**: kiểm tra inbound TCP 80 trong Security Group và chạy `docker compose ps` trên EC2.

# LTWEB1

Ứng dụng Spring Boot + PostgreSQL, đóng gói bằng Docker và chạy cùng PostgreSQL bằng Docker Compose.

## Tính năng

- Tìm kiếm, lọc và sắp xếp truyện; theo dõi truyện, lịch sử đọc và thông báo.
- Tạo danh sách truyện riêng, xem tiến độ thành tích và tiếp tục đọc từ chương gần nhất.
- Đánh giá sao, chia sẻ truyện, tùy chỉnh giao diện/cỡ chữ khi đọc và lưu vị trí cuộn trên thiết bị.
- Gửi báo cáo truyện/chương; quản trị viên xem và đánh dấu báo cáo đã xử lý.

Các bảng cho danh sách riêng và báo cáo được Flyway tự tạo/cập nhật khi ứng dụng khởi động. Tùy chọn thanh toán chưa được tích hợp.

Xem [DEPLOY.md](DEPLOY.md) để cấu hình `.env`, triển khai lên AWS EC2 và bật Jenkins CI/CD (tùy chọn).
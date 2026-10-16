---
schema: clarification-answers/v1
status: submitted
generated_at: "2026-10-16T05:40:56.588Z"
scope: [frontend, backend, generic]
questions_file: clarification-questions.json
---

# Rearchitecture Clarification Answers

## 🖥️ Frontend

- **F1** Bạn muốn tiếp tục frontend HTML/CSS/JavaScript hiện có, hay chuyển sang một framework frontend?
  - answer: Giữ HTML/CSS/JavaScript hiện có
  - source: user
- **F2** Bạn muốn dùng hệ thống component/UI nào để xây dựng giao diện?
  - answer: _none_
  - source: skipped (required, no default — blocking gap)
- **F3** Bạn có ảnh chụp, thiết kế Figma hoặc đường dẫn giao diện mong muốn không? Có thể bỏ qua nếu muốn mình tự thiết kế dựa trên giao diện hiện có.
  - answer: _none_
  - source: skipped (required, no default — blocking gap)
- **F4** Có thương hiệu, màu sắc hoặc design system nào cần tuân theo không?
  - answer: Tiếp tục phong cách trắng-xanh tối giản hiện tại và hoàn thiện đồng bộ
  - source: user
- **F5** Bạn muốn nhắm tới tiêu chuẩn hỗ trợ truy cập nào?
  - answer: WCAG 2.1 AA
  - source: user
- **F6** Những trình duyệt hoặc thiết bị nào cần hỗ trợ?
  - answer: modern evergreen (Chrome, Firefox, Safari, Edge — latest 2 major versions)
  - source: user
- **F7** Giao diện nên thích ứng với màn hình nào?
  - answer: Mobile-first, hỗ trợ điện thoại, tablet và desktop
  - source: user
- **F8** Web cần hỗ trợ ngôn ngữ nào?
  - answer: Tiếng Việt (giao diện hiện tại)
  - source: user
- **F9** Bạn có ưu tiên cách lưu trạng thái phía trình duyệt không?
  - answer: Giao diện hiện lưu yêu thích/lịch sử bằng localStorage và gọi backend bằng fetch
  - source: user
- **F10** Bạn có muốn dùng thư viện điều hướng frontend riêng không?
  - answer: Ứng dụng hiện dùng static pages và URL endpoint Spring Boot
  - source: user

## ⚙️ Backend

- **B1** Bạn muốn giữ Spring Boot hiện tại hay nâng/chuyển backend sang nền tảng khác?
  - answer: Project pom.xml đang dùng Java 17 và Spring Boot 3.2.0
  - source: user
- **B2** Các API truyện/chương đang có cần tiếp tục hoạt động tương thích không?
  - answer: Frontend hiện tại gọi /api/books và các endpoint chương/bình luận
  - source: user
- **B3** Khi bổ sung bảng tài khoản, yêu thích, lịch sử đọc và chức năng quản trị, nên xử lý PostgreSQL hiện tại thế nào?
  - answer: Render Blueprint hiện khai báo PostgreSQL và app dùng JPA; chưa có yêu cầu chuyển DB
  - source: user
- **B4** Bạn muốn triển khai xác thực và phân quyền tài khoản theo hướng nào?
  - answer: preserve existing auth mechanism; detect from codebase
  - source: user
- **B5** Bạn có yêu cầu cụ thể về thời gian phản hồi, số người dùng đồng thời hoặc độ sẵn sàng không?
  - answer: match current production baseline; no regression
  - source: default applied

## 📋 General

- **G1** Theo bạn, những điều kiện nào chứng minh web truyện đã hoàn thành đúng yêu cầu?
  - answer: Hoàn thiện web đọc truyện tranh end-to-end gồm frontend, backend, admin, đăng ký/đăng nhập; các luồng chính hoạt động và đã kiểm tra/sửa lỗi; tiếp tục deploy bằng Jenkins/Render
  - source: user
- **G2** Có phần nào bạn muốn loại trừ khỏi đợt hoàn thiện này không?
  - answer: no explicit exclusions; agent will infer from project structure
  - source: default applied
- **G3** Nếu phát hiện test cũ bị lỗi hoặc không phù hợp, bạn muốn xử lý thế nào?
  - answer: must pass
  - source: user
- **G5** Có ràng buộc bổ sung nào về nội dung truyện, nguồn ảnh, bản quyền, chi phí hosting, bảo mật hoặc cách vận hành không?
  - answer: None beyond the decisions listed in this specification.
  - source: user

---
schema: clarification/v1
generated_at: "2026-10-16T05:41:30Z"
scope:
  - frontend
  - backend
  - generic
clarity_score: 0.83
rounds: 1
gaps:
  - F2
  - F3
  - F6
  - B4
  - B5
  - G2
  - G5
blocking_gaps:
  - F2
  - F3
---

# Scenario Clarification

## Frontend

- **Target framework**: Giữ HTML/CSS/JavaScript hiện có.
- **Component library**: Chưa chọn; working assumption là CSS tự viết, không thêm thư viện frontend.
- **Screenshots**: Không cung cấp; dùng giao diện trắng-xanh hiện tại và các trang hiện có làm tham chiếu.
- **Design system**: Tiếp tục phong cách trắng-xanh tối giản hiện tại và hoàn thiện đồng bộ.
- **Accessibility**: WCAG 2.1 AA.
- **Browser targets**: Modern evergreen (Chrome, Firefox, Safari, Edge — latest 2 major versions).
- **Responsive strategy**: Mobile-first, hỗ trợ điện thoại, tablet và desktop.
- **i18n locales**: Tiếng Việt.
- **State management**: Giữ JavaScript hiện tại, chuyển dữ liệu quan trọng sang backend.
- **Routing**: Giữ điều hướng URL/link hiện tại của Spring Boot.

## Backend

- **Target framework**: Java 17 và Spring Boot 3.2.0 hiện tại.
- **API contract preservation**: Giữ tương thích các API truyện/chương/bình luận hiện có.
- **Data migration strategy**: Giữ PostgreSQL Render, cập nhật schema tại chỗ và bảo toàn dữ liệu.
- **Auth framework**: Chưa chỉ định; chọn xác thực phía backend với mật khẩu băm và vai trò USER/ADMIN.
- **SLA targets**: Khớp baseline hiện tại, không giảm chất lượng.

## Generic

- **Success definition**: Hoàn thiện web đọc truyện tranh gồm frontend, backend, admin, đăng ký/đăng nhập; các luồng chính hoạt động và đã kiểm tra/sửa lỗi; tiếp tục deploy bằng Jenkins/Render.
- **Out of scope**: Không có loại trừ cụ thể; suy ra từ cấu trúc hiện tại.
- **Existing test posture**: Tất cả test hiện có phải qua; chỉ sửa lỗi có nguyên nhân xác định.
- **Additional constraints**: Không có ràng buộc nào ngoài các quyết định trong đặc tả.

## Gaps & Defaults Applied

- id: F2
  resolution: blocking
  note: "Chưa chọn component library; trong lúc tiếp tục, dùng CSS tự viết để duyệt thư viện hiện có."
- id: F3
  resolution: blocking
  note: "Không cung cấp ảnh tham khảo; dùng giao diện trắng-xanh hiện tại làm nguồn tham chiếu."
- id: F6
  resolution: default
  default_used: "modern evergreen (Chrome, Firefox, Safari, Edge — latest 2 major versions)"
- id: B4
  resolution: default
  default_used: "Xác thực backend với mật khẩu băm, session cookie bảo mật và vai trò USER/ADMIN."
- id: B5
  resolution: default
  default_used: "Khớp baseline hiện tại; không giảm chất lượng."
- id: G2
  resolution: default
  default_used: "Không có loại trừ cụ thể; suy ra từ cấu trúc dự án."
- id: G5
  resolution: user_skipped
  default_used: "Không có ràng buộc bổ sung."

## Downstream Usage Notes

- Người dùng cho phép tự quyết định triển khai toàn bộ web truyện tranh và yêu cầu tiếp tục dùng Jenkins/Render, không chuyển sang máy ảo.
- Chưa có ảnh tham khảo hoặc lựa chọn component library; dùng CSS tự viết và thiết kế hiện có làm giả định.
- Xác thực hiện tại phía trình duyệt không được xem là bảo mật; cần thay bằng xác thực/phân quyền backend trước khi dùng thực tế.

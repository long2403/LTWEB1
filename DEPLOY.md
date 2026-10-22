# Deploy LTWEB1 lên AWS EC2 bằng Docker Compose

Ứng dụng là một Spring Boot app phục vụ cả giao diện web và API, dùng PostgreSQL. Compose chạy hai container: app và PostgreSQL. Database chỉ nằm trong mạng nội bộ của Docker; không mở cổng 5432 trên EC2.

> Đây là cấu hình HTTP phù hợp để thử nghiệm. Với website công khai, hãy cấu hình HTTPS qua domain và reverse proxy/load balancer trước khi bật `SESSION_COOKIE_SECURE=true`.

## 1. Tạo EC2

Tạo EC2 Ubuntu 22.04 hoặc mới hơn, nên dùng ít nhất 2 GiB RAM vì Docker build chạy Maven trên máy EC2. Chọn dung lượng ổ đĩa đủ cho Docker image và dữ liệu database (tối thiểu 16 GiB là điểm bắt đầu hợp lý).

Trong Security Group, chỉ mở:

- SSH (TCP 22) từ IP của bạn.
- HTTP (TCP 80) từ nơi cần truy cập website.

Không mở PostgreSQL (TCP 5432) ra Internet. Lưu file key pair `.pem` an toàn; không đưa key vào repository hoặc Jenkins log.

## 2. Kết nối từ Windows

Mở PowerShell trên máy Windows và thay đường dẫn key cùng địa chỉ IP:

```powershell
ssh -i "C:\path\to\ec2-key.pem" ubuntu@EC2_PUBLIC_IP
```

Nếu Windows từ chối quyền truy cập key, giới hạn ACL cho tài khoản hiện tại:

```powershell
icacls "C:\path\to\ec2-key.pem" /inheritance:r
icacls "C:\path\to\ec2-key.pem" /grant:r "$($env:USERNAME):(R)"
```

## 3. Cài Docker Engine và Compose

Chạy các lệnh sau trong phiên SSH trên EC2 Ubuntu:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker "$USER"
```

Đăng xuất SSH rồi đăng nhập lại để nhóm `docker` có hiệu lực. Xác nhận cài đặt:

```bash
docker --version
docker compose version
```

## 4. Clone và cấu hình

```bash
git clone https://github.com/long2403/LTWEB1.git
cd LTWEB1
cp .env.example .env
nano .env
```

Đặt giá trị riêng, mạnh và không dùng lại cho `POSTGRES_PASSWORD` và `APP_ADMIN_PASSWORD` trong `.env`. Có thể tạo mật khẩu database an toàn với `openssl rand -hex 32`. Giữ `SESSION_COOKIE_SECURE=false` khi truy cập HTTP; bật thành `true` sau khi cấu hình HTTPS. Tệp `.env` đã được loại khỏi Git và Docker build context.

Khởi động lần đầu:

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

Mở `http://EC2_PUBLIC_IP/`. Spring Boot tự chạy Flyway migrations khi khởi động.

## 5. Cập nhật phiên bản

```bash
cd ~/LTWEB1
git pull --ff-only origin main
docker compose up -d --build
docker compose ps
```

Xem log:

```bash
docker compose logs --tail=200 app
docker compose logs --tail=200 db
```

PostgreSQL lưu dữ liệu trong volume `postgres_data`, nên việc rebuild/recreate container không xóa database. Sao lưu dữ liệu trước khi triển khai migration hoặc thao tác quản trị lớn. `docker compose down -v` sẽ xóa volume database; không chạy lệnh này nếu cần giữ dữ liệu.

## 6. Jenkins CI/CD (tùy chọn)

Pipeline trong `Jenkinsfile` build ứng dụng bằng Maven; sau khi build thành công, nhánh `main` kết nối SSH vào EC2 để pull code và chạy lại Compose. EC2 cần được cài Docker Compose, clone repository vào `~/LTWEB1`, và có thể pull repository từ GitHub. Nếu repository private, cấu hình deploy key riêng trên EC2 để đọc repository.

Trong Jenkins **Manage Jenkins → Credentials**, thêm:

- Credential loại **SSH Username with private key**, ID `ec2-deploy-key`: user SSH của EC2 (thường là `ubuntu`) và private key dùng để Jenkins kết nối EC2; public key tương ứng phải được cấp quyền SSH trên EC2.
- Credential loại **Secret text**, ID `ec2-host`: public IP hoặc hostname của EC2.

Tạo job **Pipeline** dùng **Pipeline script from SCM**, trỏ tới repository này, chọn branch `main` và đặt Script Path là `Jenkinsfile`. Cấu hình public key cho user EC2 tương ứng với credential SSH; Jenkins agent cần có `ssh` và quyền chạy Maven Wrapper.

Không lưu private key hoặc `.env` trong source code.

## Xử lý lỗi thường gặp

- **Không vào được website:** kiểm tra trạng thái EC2 và inbound TCP 80 trong Security Group.
- **Container app restart liên tục:** xem `docker compose logs --tail=200 app`; xác nhận `.env` có đủ thông tin DB và tài khoản quản trị.
- **App chưa kết nối được database:** chạy `docker compose ps` và `docker compose logs --tail=200 db`; app đợi healthcheck PostgreSQL trước khi khởi động.
- **Jenkins không deploy được:** kiểm tra credential IDs, quyền SSH của user trên EC2, đường dẫn `~/LTWEB1`, và quyền đọc repository từ EC2.

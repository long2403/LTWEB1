# 🚀 Hướng dẫn Deploy lên Render

## 📋 Yêu cầu trước khi deploy
- ✅ Có tài khoản Render (https://render.com)
- ✅ Có GitHub repo với code này
- ✅ Project đã được push lên GitHub

---

## 🔧 Các bước deploy

### **Bước 1: Tạo GitHub Repository**
```bash
# Khởi tạo git (nếu chưa)
git init
git add .
git commit -m "Initial commit - Ready for Render"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/lab1.git
git push -u origin main
```

### **Bước 2: Tạo Project trên Render**
1. Truy cập https://dashboard.render.com/
2. Chọn **"New +" → "Web Service"**
3. Kết nối GitHub repository
4. Cấu hình:
   - **Name**: `lab1-app`
   - **Runtime**: `Java`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -Dserver.port=$PORT -jar target/lab1-0.0.1-SNAPSHOT.jar`
   - **Environment**: Free tier

### **Bước 3: Tạo PostgreSQL Database trên Render**
1. Chọn **"New +" → "PostgreSQL"**
2. Cấu hình:
   - **Name**: `lab1_postgres`
   - **Database**: `lab1_db`
   - **User**: Tự động (lưu lại)
   - **Region**: Gần nhất (VN)
   - **Pricing Plan**: Free

### **Bước 4: Kết nối Database vào App**
1. Copy **Internal Database URL** từ PostgreSQL
2. Vào **Web Service → Environment**
3. Thêm biến môi trường:
   - `DATABASE_URL`: `postgres://...` (từ bước 1)
   - `PORT`: `8081` (tự động)

---

## ✅ Kiểm tra sau deploy
- Truy cập: `https://your-app.onrender.com/`
- API: `https://your-app.onrender.com/api/students`
- Logs: **Render Dashboard → Web Service → Logs**

---

## 🔍 Troubleshooting

### ❌ Build fail
```
Kiểm tra: Render Logs → Maven errors
```

### ❌ Database connection failed
```
1. Kiểm tra Internal Database URL có đúng không
2. Restart Web Service
3. Kiểm tra environment variables
```

### ❌ Port error
```
Render tự động gán PORT, không cần config thêm
```

---

## 📝 Ghi chú
- Database PostgreSQL miễn phí trên Render (auto backup)
- Web Service miễn phí (spin down nếu không dùng 15 phút)
- Để luôn chạy: Nâng lên paid plan

---

**Chúc bạn deploy thành công! 🎉**

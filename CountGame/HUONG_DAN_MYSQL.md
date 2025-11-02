# HƯỚNG DẪN CẤU HÌNH MYSQL CHO BATTLESHIP (XAMPP)

## 🔄 ĐÃ CHUYỂN ĐỔI THÀNH CÔNG!

Project đã được chuyển đổi từ **SQL Server** sang **MySQL (XAMPP)**

---

## 📋 CÁC BƯỚC CẤU HÌNH

### BƯỚC 1: Cài đặt và Khởi động XAMPP

1. Tải và cài đặt XAMPP từ: https://www.apachefriends.org/
2. Mở XAMPP Control Panel
3. Khởi động **Apache** và **MySQL**

### BƯỚC 2: Tạo Database

#### Cách 1: Sử dụng phpMyAdmin (Khuyến nghị)

1. Mở trình duyệt và truy cập: `http://localhost/phpmyadmin`
2. Click vào tab **"SQL"** ở menu trên
3. Copy toàn bộ nội dung file **`database_mysql.sql`** và paste vào
4. Click **"Go"** hoặc **"Thực hiện"** để chạy

#### Cách 2: Sử dụng MySQL Command Line

1. Mở XAMPP Control Panel
2. Click **"Shell"** để mở terminal
3. Chạy các lệnh sau:

```bash
mysql -u root -p
# Nhấn Enter (không cần password nếu XAMPP mặc định)
```

4. Trong MySQL shell:
```sql
source F:/K1_N4_ME/network_programming/BTL/Battleship-Java-Socket/Code/Battle-Ship/database_mysql.sql
```

Hoặc copy-paste nội dung file SQL vào terminal.

### BƯỚC 3: Kiểm tra Database đã tạo thành công

Trong phpMyAdmin hoặc MySQL shell, chạy:

```sql
USE battleship;
SHOW TABLES;
SELECT * FROM players;
SELECT * FROM matches;
```

Bạn sẽ thấy:
- Database **battleship** 
- 2 bảng: **players** và **matches**
- 5 tài khoản test đã được tạo sẵn

---

## 🔐 THÔNG TIN KẾT NỐI

```
Database Name: battleship
Host: localhost
Port: 3306
Username: root
Password: (để trống - XAMPP mặc định không có password)
```

---

## ✅ TÀI KHOẢN TEST ĐÃ TẠO SẴN

Bạn có thể login bằng các tài khoản sau:

| Username  | Password  | Điểm | Wins | Losses |
|-----------|-----------|------|------|--------|
| admin     | admin123  | 100  | 50   | 30     |
| player1   | pass123   | 85   | 42   | 35     |
| player2   | pass123   | 72   | 38   | 40     |
| testuser  | test123   | 50   | 25   | 25     |
| demo      | demo123   | 30   | 15   | 20     |

---

## 🚀 CHẠY PROJECT

### 1. Compile Project với MySQL Driver mới

```powershell
cd "F:\K1_N4_ME\network_programming\BTL\Battleship-Java-Socket\Code\Battle-Ship"
.\mvnw.cmd clean compile
```

### 2. Chạy Server

```powershell
.\mvnw.cmd exec:java -Dexec.mainClass="server.view.ServerMainFrm"
```

### 3. Chạy Client (Terminal mới)

```powershell
.\mvnw.cmd javafx:run
```

---

## 📊 CẤU TRÚC DATABASE

### Bảng: **players**
```
- id (INT, AUTO_INCREMENT, PRIMARY KEY)
- username (VARCHAR(50), UNIQUE, NOT NULL)
- password (VARCHAR(255), NOT NULL)
- points (INT, DEFAULT 0)
- total_wins (INT, DEFAULT 0)
- total_losses (INT, DEFAULT 0)
- total_afk (INT, DEFAULT 0)
- total_draw (INT, DEFAULT 0)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Bảng: **matches**
```
- id (INT, AUTO_INCREMENT, PRIMARY KEY)
- user1_username (VARCHAR(50), FOREIGN KEY)
- user2_username (VARCHAR(50), FOREIGN KEY)
- result_user1 (VARCHAR(20))
- result_user2 (VARCHAR(20))
- points_change_user1 (INT)
- points_change_user2 (INT)
- timestamp (TIMESTAMP)
```

---

## ⚙️ THAY ĐỔI ĐÃ THỰC HIỆN

### 1. File: `pom.xml`
- ✅ Thêm MySQL Connector (mysql-connector-java 8.0.33)
- ❌ Loại bỏ SQL Server JDBC (mssql-jdbc)

### 2. File: `server/dao/DAO.java`
- ✅ Đổi connection string từ SQL Server sang MySQL
- ✅ Thay đổi port: 1433 → 3306
- ✅ Thay đổi database: bantau → battleship
- ✅ Thay đổi user: sa → root
- ✅ Password: để trống (XAMPP default)

### 3. File mới: `database_mysql.sql`
- ✅ Tạo database battleship
- ✅ Tạo bảng players
- ✅ Tạo bảng matches
- ✅ Thêm dữ liệu mẫu

---

## 🔧 XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi: "Access denied for user 'root'@'localhost'"
**Giải pháp:**
1. Mở phpMyAdmin
2. Đi tới User accounts
3. Kiểm tra password của user root
4. Cập nhật password trong file `DAO.java` nếu cần

### Lỗi: "Unknown database 'battleship'"
**Giải pháp:**
- Chạy lại file `database_mysql.sql` trong phpMyAdmin

### Lỗi: "Communications link failure"
**Giải pháp:**
- Kiểm tra MySQL đã chạy trong XAMPP chưa
- Kiểm tra port 3306 không bị chặn bởi firewall

### Lỗi: "No suitable driver found"
**Giải pháp:**
```powershell
.\mvnw.cmd clean install
```

---

## 📝 LƯU Ý

1. **Phải khởi động MySQL trong XAMPP trước khi chạy Server**
2. **Port mặc định MySQL**: 3306 (có thể thay đổi trong XAMPP config)
3. **Dữ liệu sẽ được lưu vĩnh viễn** trong MySQL, không mất khi tắt XAMPP
4. **Backup database thường xuyên** bằng cách export từ phpMyAdmin

---

## 🎯 KIỂM TRA KẾT NỐI

Khi chạy Server, nếu kết nối thành công bạn sẽ thấy:
```
Kết nối MySQL thành công!
```

Nếu thất bại, bạn sẽ thấy:
```
Lỗi kết nối MySQL!
```

---

**Chúc bạn thành công! 🚀**


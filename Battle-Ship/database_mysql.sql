-- ================================================
-- DATABASE BATTLESHIP - MYSQL (XAMPP)
-- ================================================
-- Tạo database battleship
-- ================================================

-- Xóa database cũ nếu tồn tại
DROP DATABASE IF EXISTS battleship;

-- Tạo database mới
CREATE DATABASE battleship CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Sử dụng database
USE battleship;

-- ================================================
-- Bảng PLAYERS - Lưu thông tin người chơi
-- ================================================
CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    points INT DEFAULT 0,
    total_wins INT DEFAULT 0,
    total_losses INT DEFAULT 0,
    total_afk INT DEFAULT 0,
    total_draw INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_points (points DESC)
) ENGINE=InnoDB;

-- ================================================
-- Bảng MATCHES - Lưu lịch sử các trận đấu
-- ================================================
CREATE TABLE matches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1_username VARCHAR(50) NOT NULL,
    user2_username VARCHAR(50) NOT NULL,
    result_user1 VARCHAR(20) NOT NULL,
    result_user2 VARCHAR(20) NOT NULL,
    points_change_user1 INT DEFAULT 0,
    points_change_user2 INT DEFAULT 0,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_username) REFERENCES players(username) ON DELETE CASCADE,
    FOREIGN KEY (user2_username) REFERENCES players(username) ON DELETE CASCADE,
    INDEX idx_user1 (user1_username),
    INDEX idx_user2 (user2_username),
    INDEX idx_timestamp (timestamp DESC)
) ENGINE=InnoDB;

-- ================================================
-- Dữ liệu mẫu - Tạo một số tài khoản test
-- ================================================
INSERT INTO players (username, password, points, total_wins, total_losses, total_afk, total_draw) VALUES
('admin', 'admin123', 100, 50, 30, 5, 10),
('player1', 'pass123', 85, 42, 35, 3, 8),
('player2', 'pass123', 72, 38, 40, 2, 5),
('testuser', 'test123', 50, 25, 25, 1, 3),
('demo', 'demo123', 30, 15, 20, 0, 2);

-- ================================================
-- Dữ liệu mẫu - Tạo lịch sử trận đấu
-- ================================================
INSERT INTO matches (user1_username, user2_username, result_user1, result_user2, points_change_user1, points_change_user2) VALUES
('admin', 'player1', 'WIN', 'LOSS', 1, 0),
('player2', 'testuser', 'WIN', 'LOSS', 1, 0),
('admin', 'player2', 'LOSS', 'WIN', 0, 1),
('player1', 'demo', 'WIN', 'LOSS', 1, 0),
('testuser', 'demo', 'DRAW', 'DRAW', 0, 0);

-- ================================================
-- Hiển thị thông tin các bảng đã tạo
-- ================================================
SHOW TABLES;

-- Kiểm tra dữ liệu
SELECT 'Danh sách người chơi:' as '';
SELECT username, points, total_wins, total_losses, total_afk, total_draw FROM players ORDER BY points DESC;

SELECT 'Lịch sử trận đấu:' as '';
SELECT id, user1_username, user2_username, result_user1, result_user2, timestamp FROM matches ORDER BY timestamp DESC LIMIT 10;

-- ================================================
-- HOÀN TẤT!
-- ================================================


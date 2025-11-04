-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 04, 2025 lúc 12:09 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `countgame`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `matches`
--

CREATE TABLE `matches` (
  `match_id` int(11) NOT NULL,
  `user1_username` varchar(50) NOT NULL,
  `user2_username` varchar(50) NOT NULL,
  `timestamp` datetime DEFAULT current_timestamp(),
  `result_user1` enum('win','loss','afk','cancelled','draw') DEFAULT NULL,
  `result_user2` enum('win','loss','afk','cancelled','draw') DEFAULT NULL,
  `points_change_user1` int(11) DEFAULT NULL CHECK (`points_change_user1` in (1,0,-1)),
  `points_change_user2` int(11) DEFAULT NULL CHECK (`points_change_user2` in (1,0,-1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `matches`
--

INSERT INTO `matches` (`match_id`, `user1_username`, `user2_username`, `timestamp`, `result_user1`, `result_user2`, `points_change_user1`, `points_change_user2`) VALUES
(1, 'test2', 'test1', '2025-11-02 22:55:18', 'draw', 'draw', 0, 0),
(2, 'test3', 'test1', '2025-11-02 23:56:28', 'win', 'loss', 1, 0),
(3, 'test3', 'test1', '2025-11-02 23:58:00', 'draw', 'draw', 0, 0),
(4, 'test1', 'test3', '2025-11-02 23:59:59', 'win', 'loss', 1, 0),
(5, 'test3', 'test1', '2025-11-03 00:11:55', 'draw', 'draw', 0, 0),
(6, 'test1', 'test2', '2025-11-03 00:45:57', 'win', 'afk', 1, -1),
(7, 'test1', 'test2', '2025-11-03 00:49:58', 'win', 'afk', 1, -1),
(8, 'test3', 'test2', '2025-11-03 00:54:39', 'win', 'afk', 1, -1),
(9, 'test1', 'test2', '2025-11-03 01:27:27', 'win', 'afk', 1, -1),
(10, 'test1', 'test2', '2025-11-03 08:08:14', 'win', 'loss', 1, 0),
(11, 'test1', 'test2', '2025-11-03 08:13:14', 'win', 'afk', 1, -1),
(12, 'test3', 'test1', '2025-11-03 08:27:26', 'win', 'afk', 1, -1),
(13, 'test3', 'test1', '2025-11-03 08:53:25', 'draw', 'draw', 0, 0),
(14, 'test1', 'test3', '2025-11-03 10:08:52', 'draw', 'draw', 0, 0),
(15, 'test3', 'test1', '2025-11-03 10:40:33', 'draw', 'draw', 0, 0),
(16, 'test3', 'test1', '2025-11-03 11:09:30', 'draw', 'draw', 0, 0),
(17, 'test3', 'test1', '2025-11-03 11:24:46', 'draw', 'draw', 0, 0),
(18, 'test3', 'test1', '2025-11-03 15:04:26', 'draw', 'draw', 0, 0),
(19, 'test3', 'test1', '2025-11-03 15:27:40', 'draw', 'draw', 0, 0),
(20, 'test3', 'test1', '2025-11-03 15:52:29', 'draw', 'draw', 0, 0),
(21, 'test3', 'test1', '2025-11-03 15:53:32', 'draw', 'draw', 0, 0),
(22, 'test3', 'test1', '2025-11-03 16:10:53', 'draw', 'draw', 0, 0),
(23, 'test3', 'test1', '2025-11-03 16:27:22', 'win', 'loss', 1, 0),
(24, 'test3', 'test1', '2025-11-03 16:33:53', 'win', 'loss', 1, 0),
(25, 'test1', 'test3', '2025-11-03 16:35:43', 'win', 'loss', 1, 0),
(26, 'test1', 'test3', '2025-11-03 18:04:38', 'draw', 'draw', 0, 0),
(27, 'test1', 'test3', '2025-11-03 18:06:11', 'draw', 'draw', 0, 0),
(28, 'test3', 'test1', '2025-11-03 20:43:32', 'win', 'loss', 1, 0),
(29, 'test1', 'test3', '2025-11-03 20:57:00', 'draw', 'draw', 0, 0),
(30, 'test3', 'test1', '2025-11-03 21:11:06', 'win', 'loss', 1, 0),
(31, 'test3', 'test1', '2025-11-03 21:11:33', 'win', 'loss', 1, 0),
(32, 'test3', 'test1', '2025-11-03 21:12:29', 'draw', 'draw', 0, 0),
(33, 'test1', 'test3', '2025-11-03 21:13:49', 'draw', 'draw', 0, 0),
(34, 'test3', 'test1', '2025-11-03 21:26:50', 'win', 'loss', 1, 0),
(35, 'test3', 'test1', '2025-11-03 21:27:45', 'draw', 'draw', 0, 0),
(36, 'test1', 'test3', '2025-11-03 21:38:01', 'win', 'loss', 1, 0),
(37, 'test3', 'test1', '2025-11-03 21:38:56', 'draw', 'draw', 0, 0),
(38, 'test3', 'test1', '2025-11-03 21:50:57', 'win', 'loss', 1, 0),
(39, 'test3', 'test1', '2025-11-03 21:57:53', 'draw', 'draw', 0, 0),
(40, 'test1', 'test3', '2025-11-03 21:58:25', 'win', 'loss', 1, 0),
(41, 'test4', 'test2', '2025-11-03 22:33:22', 'draw', 'draw', 0, 0),
(42, 'test2', 'test4', '2025-11-03 22:33:50', 'win', 'afk', 1, -1),
(43, 'test4', 'test2', '2025-11-03 22:35:59', 'draw', 'draw', 0, 0),
(44, 'test2', 'test1', '2025-11-03 22:36:47', 'draw', 'draw', 0, 0),
(45, 'test4', 'test2', '2025-11-03 22:41:37', 'win', 'loss', 1, 0),
(46, 'test4', 'test2', '2025-11-03 22:42:32', 'win', 'loss', 1, 0),
(47, 'test2', 'test4', '2025-11-03 22:42:57', 'win', 'loss', 1, 0),
(48, 'test4', 'test1', '2025-11-03 22:51:46', 'win', 'loss', 1, 0),
(49, 'test1', 'test4', '2025-11-03 22:52:15', 'draw', 'draw', 0, 0),
(50, 'test4', 'test1', '2025-11-03 22:52:50', 'win', 'afk', 1, -1),
(51, 'test1', 'test3', '2025-11-03 23:19:26', 'win', 'loss', 1, 0),
(52, 'test1', 'test4', '2025-11-03 23:31:41', 'win', 'loss', 1, 0),
(53, 'test1', 'test2', '2025-11-03 23:42:49', 'win', 'loss', 1, 0),
(54, 'test2', 'test1', '2025-11-03 23:58:56', 'win', 'loss', 1, 0),
(55, 'test1', 'test2', '2025-11-04 00:26:53', 'draw', 'draw', 0, 0),
(56, 'test2', 'test1', '2025-11-04 00:27:45', 'draw', 'draw', 0, 0),
(57, 'test3', 'test1', '2025-11-04 00:43:21', 'win', 'loss', 1, 0),
(58, 'test3', 'test1', '2025-11-04 00:43:47', 'win', 'afk', 1, -1),
(59, 'test2', 'test1', '2025-11-04 01:05:12', 'draw', 'draw', 0, 0),
(60, 'test2', 'test1', '2025-11-04 01:05:41', 'win', 'loss', 1, 0),
(61, 'test2', 'test1', '2025-11-04 01:06:20', 'win', 'loss', 1, 0),
(62, 'test2', 'test1', '2025-11-04 01:06:52', 'win', 'loss', 1, 0),
(63, 'test1', 'test2', '2025-11-04 01:07:59', 'draw', 'draw', 0, 0),
(64, 'test3', 'test1', '2025-11-04 08:45:23', 'win', 'loss', 1, 0),
(65, 'test1', 'test3', '2025-11-04 08:45:46', 'win', 'afk', 1, -1),
(66, 'test3', 'test1', '2025-11-04 08:49:08', 'win', 'afk', 1, -1),
(67, 'test1', 'test3', '2025-11-04 08:56:02', 'win', 'afk', 1, -1),
(68, 'test3', 'test1', '2025-11-04 09:04:15', 'win', 'afk', 1, -1),
(69, 'test3', 'test1', '2025-11-04 09:44:45', 'win', 'afk', 1, -1),
(70, 'test1', 'test3', '2025-11-04 09:50:51', 'win', 'afk', 1, -1),
(71, 'test1', 'test3', '2025-11-04 09:56:20', 'draw', 'draw', 0, 0),
(72, 'test3', 'test1', '2025-11-04 10:08:22', 'win', 'afk', 1, -1),
(73, 'test3', 'test1', '2025-11-04 10:12:24', 'win', 'afk', 1, -1),
(74, 'test1', 'test3', '2025-11-04 10:20:03', 'win', 'afk', 1, -1),
(75, 'test1', 'test3', '2025-11-04 10:27:00', 'draw', 'draw', 0, 0),
(76, 'test1', 'test3', '2025-11-04 10:31:34', 'win', 'afk', 1, -1),
(77, 'test3', 'test1', '2025-11-04 10:33:29', 'win', 'loss', 1, 0),
(78, 'test3', 'test1', '2025-11-04 10:34:22', 'win', 'loss', 1, 0),
(79, 'test3', 'test1', '2025-11-04 10:35:20', 'win', 'afk', 1, -1),
(80, 'test1', 'test3', '2025-11-04 10:37:10', 'draw', 'draw', 0, 0),
(81, 'test1', 'test3', '2025-11-04 10:41:58', 'win', 'afk', 1, -1),
(82, 'test1', 'test3', '2025-11-04 10:54:05', 'win', 'afk', 1, -1),
(83, 'test3', 'test1', '2025-11-04 10:54:39', 'win', 'loss', 1, 0),
(84, 'test1', 'test3', '2025-11-04 11:05:09', 'win', 'loss', 1, 0),
(85, 'test1', 'test3', '2025-11-04 11:06:12', 'draw', 'draw', 0, 0),
(86, 'test3', 'test1', '2025-11-04 11:08:35', 'draw', 'draw', 0, 0),
(87, 'test1', 'test3', '2025-11-04 11:14:30', 'draw', 'draw', 0, 0),
(88, 'test3', 'test1', '2025-11-04 11:15:23', 'win', 'loss', 1, 0),
(89, 'test1', 'test3', '2025-11-04 11:18:28', 'win', 'loss', 1, 0),
(90, 'test3', 'test1', '2025-11-04 11:33:08', 'win', 'loss', 1, 0),
(91, 'test3', 'test1', '2025-11-04 11:35:08', 'draw', 'draw', 0, 0),
(92, 'test1', 'test3', '2025-11-04 11:38:38', 'draw', 'draw', 0, 0),
(93, 'test3', 'test1', '2025-11-04 11:38:54', 'win', 'afk', 1, -1),
(94, 'test1', 'test3', '2025-11-04 11:39:16', 'win', 'loss', 1, 0),
(95, 'test1', 'test3', '2025-11-04 12:43:42', 'draw', 'draw', 0, 0),
(96, 'test3', 'test1', '2025-11-04 12:44:01', 'win', 'afk', 1, -1),
(97, 'test1', 'test3', '2025-11-04 12:48:35', 'draw', 'draw', 0, 0),
(98, 'test3', 'test1', '2025-11-04 14:50:22', 'draw', 'draw', 0, 0),
(99, 'test1', 'test3', '2025-11-04 14:50:53', 'draw', 'draw', 0, 0),
(100, 'test3', 'test1', '2025-11-04 14:58:04', 'draw', 'draw', 0, 0),
(101, 'test1', 'test3', '2025-11-04 14:58:38', 'win', 'afk', 1, -1),
(102, 'test1', 'test3', '2025-11-04 14:58:58', 'win', 'loss', 1, 0),
(103, 'test1', 'test3', '2025-11-04 14:59:27', 'win', 'loss', 1, 0),
(104, 'test1', 'test3', '2025-11-04 14:59:53', 'win', 'loss', 1, 0),
(105, 'test3', 'test1', '2025-11-04 15:01:58', 'draw', 'draw', 0, 0),
(106, 'test1', 'test3', '2025-11-04 15:03:33', 'win', 'loss', 1, 0),
(107, 'test3', 'test1', '2025-11-04 15:08:15', 'win', 'loss', 1, 0),
(108, 'test3', 'test1', '2025-11-04 15:08:47', 'win', 'loss', 1, 0),
(109, 'test3', 'test1', '2025-11-04 15:15:00', 'draw', 'draw', 0, 0),
(110, 'test1', 'test3', '2025-11-04 15:15:16', 'win', 'loss', 1, 0),
(111, 'test3', 'test1', '2025-11-04 15:15:40', 'draw', 'draw', 0, 0),
(112, 'test3', 'test1', '2025-11-04 15:15:54', 'win', 'afk', 1, -1),
(113, 'test1', 'test3', '2025-11-04 15:44:47', 'win', 'loss', 1, 0),
(114, 'test3', 'test1', '2025-11-04 15:45:11', 'win', 'loss', 1, 0),
(115, 'test3', 'test1', '2025-11-04 15:55:48', 'win', 'loss', 1, 0),
(116, 'test3', 'test1', '2025-11-04 16:03:50', 'win', 'loss', 1, 0),
(117, 'test3', 'test1', '2025-11-04 16:04:26', 'win', 'loss', 1, 0),
(118, 'test3', 'test1', '2025-11-04 16:04:39', 'win', 'afk', 1, -1),
(119, 'test3', 'test1', '2025-11-04 16:40:23', 'win', 'afk', 1, -1),
(120, 'test3', 'test1', '2025-11-04 16:40:52', 'win', 'loss', 1, 0),
(121, 'test3', 'test1', '2025-11-04 16:41:22', 'win', 'loss', 1, 0),
(122, 'Dung', 'LeTuong', '2025-11-04 17:46:03', 'win', 'loss', 1, 0),
(123, 'Dung', 'LeTuong', '2025-11-04 17:48:06', 'win', 'loss', 1, 0),
(124, 'LeTuong', 'test3', '2025-11-04 17:51:07', 'win', 'afk', 1, -1),
(125, 'LeTuong', 'Dung', '2025-11-04 17:59:36', 'draw', 'draw', 0, 0),
(126, 'LeTuong', 'Dung', '2025-11-04 18:02:21', 'win', 'loss', 1, 0),
(127, 'Dung', 'LeTuong', '2025-11-04 18:02:46', 'win', 'loss', 1, 0);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `players`
--

CREATE TABLE `players` (
  `username` varchar(50) NOT NULL,
  `password` varchar(50) DEFAULT NULL,
  `points` int(11) DEFAULT 0,
  `total_wins` int(11) DEFAULT 0,
  `total_losses` int(11) DEFAULT 0,
  `total_afk` int(11) DEFAULT 0,
  `total_draw` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `players`
--

INSERT INTO `players` (`username`, `password`, `points`, `total_wins`, `total_losses`, `total_afk`, `total_draw`) VALUES
('Dung', '1', 67, 83, 11, 5, 11),
('LeTuong', '1', 19, 16, 8, 0, 6),
('test1', '1', 7, 28, 39, 14, 43),
('test2', '2', 1, 6, 9, 5, 8),
('test3', '3', 22, 34, 21, 9, 36),
('test4', '4', 0, 0, 0, 0, 0),
('test5', '5', 0, 0, 0, 0, 0),
('test6', '6', 0, 0, 0, 0, 0),
('test7', '7', 0, 0, 0, 0, 0),
('Thien', '1', 41, 50, 10, 2, 5),
('Tien', '1', 37, 49, 8, 4, 23);

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `matches`
--
ALTER TABLE `matches`
  ADD PRIMARY KEY (`match_id`),
  ADD KEY `user1_username` (`user1_username`),
  ADD KEY `user2_username` (`user2_username`);

--
-- Chỉ mục cho bảng `players`
--
ALTER TABLE `players`
  ADD PRIMARY KEY (`username`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `matches`
--
ALTER TABLE `matches`
  MODIFY `match_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=128;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `matches`
--
ALTER TABLE `matches`
  ADD CONSTRAINT `matches_ibfk_1` FOREIGN KEY (`user1_username`) REFERENCES `players` (`username`),
  ADD CONSTRAINT `matches_ibfk_2` FOREIGN KEY (`user2_username`) REFERENCES `players` (`username`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

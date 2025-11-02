CREATE TABLE players (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50),
    points INT DEFAULT 0,
    total_wins INT DEFAULT 0,
    total_losses INT DEFAULT 0,
    total_afk INT DEFAULT 0,
    total_draw INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE matches (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    user1_username VARCHAR(50) NOT NULL,
    user2_username VARCHAR(50) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    result_user1 ENUM('win', 'loss', 'afk', 'cancelled', 'draw'),
    result_user2 ENUM('win', 'loss', 'afk', 'cancelled', 'draw'),
    points_change_user1 INT CHECK (points_change_user1 IN (1, 0, -1)),
    points_change_user2 INT CHECK (points_change_user2 IN (1, 0, -1)),
    FOREIGN KEY (user1_username) REFERENCES players(username),
    FOREIGN KEY (user2_username) REFERENCES players(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO players (username, password)
VALUES 
('test1', '1'),
('test2', '2'),
('test3', '3');

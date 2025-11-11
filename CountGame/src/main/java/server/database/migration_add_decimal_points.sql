-- Migration script to change points column from INT to DECIMAL to support 0.5 points for draws
-- Run this script on your database to enable 0.5 point support

ALTER TABLE players MODIFY COLUMN points DECIMAL(10,1) DEFAULT 0;


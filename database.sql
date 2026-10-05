-- ============================================================
--  Personal Knowledge Vault - MySQL Database Script
--  Run this file ONE TIME before starting the Java project.
--  You can run it in MySQL Workbench or in the mysql client.
-- ============================================================

CREATE DATABASE IF NOT EXISTS personal_knowledge_vault;
USE personal_knowledge_vault;

-- 1. users table
CREATE TABLE users (
    user_id   INT PRIMARY KEY AUTO_INCREMENT,
    username  VARCHAR(50) NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    email     VARCHAR(100)
);

-- 2. categories table
CREATE TABLE categories (
    category_id   INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE
);

-- 3. notes table
CREATE TABLE notes (
    note_id     INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT,
    title       VARCHAR(200) NOT NULL,
    content     TEXT,
    category_id INT,
    favorite    BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- 4. resources table
CREATE TABLE resources (
    resource_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT,
    title       VARCHAR(200) NOT NULL,
    url         VARCHAR(500) NOT NULL,
    description TEXT,
    category_id INT,
    favorite    BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- Default categories
INSERT INTO categories (category_name) VALUES
('Programming'),
('AI/ML'),
('Mathematics'),
('Physics'),
('Chemistry'),
('Personal');

-- Optional demo user (username = student, password = 123456)
INSERT INTO users (username, password, email) VALUES
('student', '123456', 'student@example.com');

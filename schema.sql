-- Daily Budget Planner Database Schema
-- Run this script in MySQL Workbench or mysql CLI:
-- mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS budget_planner;
USE budget_planner;

-- 1. users table
CREATE TABLE IF NOT EXISTS users (
    userId INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. plans table
CREATE TABLE IF NOT EXISTS plans (
    planId INT NOT NULL AUTO_INCREMENT,
    userId INT NOT NULL,
    month VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (planId),
    UNIQUE KEY uq_user_month_year (userId, month, year),
    KEY idx_user (userId),
    CONSTRAINT fk_plans_user FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. spend_amount table (day-by-day running budget and limits)
CREATE TABLE IF NOT EXISTS spend_amount (
    spendId INT NOT NULL AUTO_INCREMENT,
    planId INT NOT NULL,
    day INT NOT NULL,
    day_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    current_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    daily_budget DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (spendId),
    UNIQUE KEY uq_plan_day (planId, day),
    KEY idx_plan (planId),
    CONSTRAINT fk_spend_plan FOREIGN KEY (planId) REFERENCES plans(planId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. spended_day table (individual expenditures / transactions)
CREATE TABLE IF NOT EXISTS spended_day (
    dayId INT NOT NULL AUTO_INCREMENT,
    spendId INT NOT NULL,
    day INT NOT NULL,
    spended_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    note VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (dayId),
    KEY idx_spend (spendId),
    CONSTRAINT fk_spended_spend FOREIGN KEY (spendId) REFERENCES spend_amount(spendId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
-- Safe migration commands for pre-existing databases:
-- -------------------------------------------------------------
-- If migrating an existing database without dropping data:
-- ALTER TABLE plans ADD CONSTRAINT uq_user_month_year UNIQUE (userId, month, year);
-- ALTER TABLE spend_amount ADD CONSTRAINT uq_plan_day UNIQUE (planId, day);

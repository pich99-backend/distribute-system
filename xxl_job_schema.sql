-- ============================================
-- XXL-JOB Database Schema Script (3.3.0)
-- For MySQL 5.7+ / MySQL 8.0+
-- ============================================

-- 1) Create the database
CREATE DATABASE IF NOT EXISTS xxl_job
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- 2) Switch to the database
USE xxl_job;

-- 3) Disable FK checks while creating tables
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- Table: xxl_job_group
-- ============================================
DROP TABLE IF EXISTS xxl_job_group;
CREATE TABLE xxl_job_group (
  id INT NOT NULL AUTO_INCREMENT,
  app_name VARCHAR(64) NOT NULL COMMENT 'Executor AppName',
  title VARCHAR(12) NOT NULL COMMENT 'Executor title',
  address_type TINYINT NOT NULL DEFAULT 0 COMMENT 'Address type: 0=auto,1=manual',
  address_list TEXT NULL COMMENT 'Executor address list',
  update_time DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- Table: xxl_job_info
-- ============================================
DROP TABLE IF EXISTS xxl_job_info;
CREATE TABLE xxl_job_info (
  id INT NOT NULL AUTO_INCREMENT,
  job_group INT NOT NULL COMMENT 'Job group ID',
  job_desc VARCHAR(255) NOT NULL,
  add_time DATETIME NULL DEFAULT NULL,
  update_time DATETIME NULL DEFAULT NULL,
  author VARCHAR(64) NULL DEFAULT NULL COMMENT 'Author',
  alarm_email VARCHAR(255) NULL DEFAULT NULL COMMENT 'Alarm email',
  alarm_status TINYINT NOT NULL DEFAULT 0 COMMENT 'Alarm status: 0=default,1=success,2=failed',
  schedule_type VARCHAR(50) NOT NULL DEFAULT 'NONE' COMMENT 'Schedule type',
  schedule_conf VARCHAR(128) NULL DEFAULT NULL,
  misfire_strategy VARCHAR(50) NOT NULL DEFAULT 'DO_NOTHING',
  executor_route_strategy VARCHAR(50) NULL DEFAULT NULL,
  executor_handler VARCHAR(255) NULL DEFAULT NULL,
  executor_param VARCHAR(512) NULL DEFAULT NULL,
  executor_block_strategy VARCHAR(50) NULL DEFAULT NULL,
  executor_timeout INT NOT NULL DEFAULT 0,
  executor_fail_retry_count INT NOT NULL DEFAULT 0,
  glue_type VARCHAR(50) NOT NULL,
  glue_source MEDIUMTEXT NULL,
  glue_remark VARCHAR(128) NULL,
  glue_updatetime DATETIME NULL DEFAULT NULL,
  child_jobid VARCHAR(255) NULL DEFAULT NULL,
  trigger_status TINYINT NOT NULL DEFAULT 0 COMMENT 'Trigger status: 0-stop,1-running',
  trigger_last_time BIGINT NOT NULL DEFAULT 0 COMMENT 'Last trigger timestamp',
  trigger_next_time BIGINT NOT NULL DEFAULT 0 COMMENT 'Next trigger timestamp',
  PRIMARY KEY (id),
  KEY idx_job_group (job_group),
  KEY idx_trigger_status_next_time (trigger_status, trigger_next_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- Table: xxl_job_log
-- ============================================
DROP TABLE IF EXISTS xxl_job_log;
CREATE TABLE xxl_job_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  job_group INT NOT NULL,
  job_id INT NOT NULL,
  executor_address VARCHAR(255) NULL,
  executor_handler VARCHAR(255) NULL,
  executor_param VARCHAR(512) NULL,
  executor_sharding_param VARCHAR(50) NULL,
  trigger_time BIGINT NOT NULL DEFAULT 0,
  trigger_code INT NOT NULL,
  trigger_msg VARCHAR(500) NULL,
  handle_time BIGINT NOT NULL DEFAULT 0,
  handle_code INT NOT NULL DEFAULT 0,
  handle_msg VARCHAR(500) NULL,
  alarm_status TINYINT NOT NULL DEFAULT 0 COMMENT 'Alarm status: 0=default,1=success,2=failed',
  PRIMARY KEY (id),
  KEY idx_job_group (job_group),
  KEY idx_job_id (job_id),
  KEY idx_trigger_time (trigger_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- Table: xxl_job_log_report
-- ============================================
DROP TABLE IF EXISTS xxl_job_log_report;
CREATE TABLE xxl_job_log_report (
  id INT NOT NULL AUTO_INCREMENT,
  trigger_day DATETIME NULL DEFAULT NULL,
  running_count INT NOT NULL DEFAULT 0,
  suc_count INT NOT NULL DEFAULT 0,
  fail_count INT NOT NULL DEFAULT 0,
  update_time DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_trigger_day (trigger_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- Table: xxl_job_logglue
-- ============================================
DROP TABLE IF EXISTS xxl_job_logglue;
CREATE TABLE xxl_job_logglue (
  id INT NOT NULL AUTO_INCREMENT,
  job_id INT NOT NULL,
  glue_type VARCHAR(50) NULL,
  glue_source MEDIUMTEXT NULL,
  glue_remark VARCHAR(128) NULL,
  add_time DATETIME NULL DEFAULT NULL,
  update_time DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_job_id (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- Table: xxl_job_registry
-- ============================================
DROP TABLE IF EXISTS xxl_job_registry;
CREATE TABLE xxl_job_registry (
  id INT NOT NULL AUTO_INCREMENT,
  registry_group VARCHAR(50) NOT NULL,
  registry_key VARCHAR(255) NOT NULL,
  registry_value VARCHAR(255) NOT NULL,
  update_time DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_g_k_v (registry_group, registry_key, registry_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- Table: xxl_job_lock
-- ============================================
DROP TABLE IF EXISTS xxl_job_lock;
CREATE TABLE xxl_job_lock (
  lock_name VARCHAR(128) NOT NULL COMMENT 'Lock name',
  PRIMARY KEY (lock_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Insert default lock
INSERT INTO `xxl_job_lock` (`lock_name`) VALUES ('schedule_lock');

-- ============================================
-- Table: xxl_job_user
-- ============================================
DROP TABLE IF EXISTS xxl_job_user;
CREATE TABLE xxl_job_user (
  id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL COMMENT 'Username',
  password VARCHAR(50) NOT NULL COMMENT 'Password',
  role TINYINT NOT NULL COMMENT 'Role: 0=user,1=admin',
  permission VARCHAR(255) NULL,
  token VARCHAR(255) NULL COMMENT 'Optional token for API authentication',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- Insert default admin user
-- Default credentials: admin / 123456
-- ============================================
INSERT INTO xxl_job_user (username, password, role, permission)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', 1, NULL)
ON DUPLICATE KEY UPDATE username = username;

-- 4) Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;


-- ============================================
-- Gulimall Database Creation Script
-- ============================================
-- This script creates all databases required by the Gulimall project
-- Execute this script with a MySQL user that has CREATE DATABASE privileges
-- ============================================

-- Product Management System Database
CREATE DATABASE IF NOT EXISTS `gulimall_pms` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Order Management System Database
CREATE DATABASE IF NOT EXISTS `gulimall_oms` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- User Management System Database
CREATE DATABASE IF NOT EXISTS `gulimall_ums` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Warehouse Management System Database
CREATE DATABASE IF NOT EXISTS `gulimall_wms` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- System Management Service Database
CREATE DATABASE IF NOT EXISTS `gulimall_sms` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Show created databases
SHOW DATABASES LIKE 'gulimall_%';

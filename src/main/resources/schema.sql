-- ================================================================
-- AegisRx Master Script: Unified Schema & Seed Data
-- ================================================================

USE master;
GO
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'aegisrx')
    CREATE DATABASE aegisrx;
GO
USE aegisrx;
GO

-- 1. DROP TABLES (Reverse Dependency Order)
DROP TABLE IF EXISTS disposal_log;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS dietary_alerts;
DROP TABLE IF EXISTS food_log;
DROP TABLE IF EXISTS drug_interactions;
DROP TABLE IF EXISTS patient_medications;
DROP TABLE IF EXISTS patient_profiles;
DROP TABLE IF EXISTS pharmacy_inventory;
DROP TABLE IF EXISTS batch_transfers;
DROP TABLE IF EXISTS medicine_batches;
DROP TABLE IF EXISTS pharmacies;
DROP TABLE IF EXISTS drug_profiles;
DROP TABLE IF EXISTS users;
GO

-- 2. CREATE TABLES
CREATE TABLE users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE drug_profiles (
    drug_id INT PRIMARY KEY IDENTITY(1,1),
    drug_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    description TEXT,
    manufacturer_id INT,
    FOREIGN KEY (manufacturer_id) REFERENCES users(user_id)
);

CREATE TABLE pharmacies (
    pharmacy_id INT PRIMARY KEY IDENTITY(1,1),
    pharmacy_name VARCHAR(100) NOT NULL,
    city VARCHAR(50),
    region VARCHAR(50),
    contact_info VARCHAR(200),
    staff_user_id INT,
    FOREIGN KEY (staff_user_id) REFERENCES users(user_id)
);

CREATE TABLE medicine_batches (
    batch_id INT PRIMARY KEY IDENTITY(1,1),
    gbi VARCHAR(50) UNIQUE NOT NULL,
    drug_id INT,
    manufacturer_id INT,
    production_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    current_holder INT,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (drug_id) REFERENCES drug_profiles(drug_id),
    FOREIGN KEY (manufacturer_id) REFERENCES users(user_id),
    FOREIGN KEY (current_holder) REFERENCES users(user_id)
);

CREATE TABLE batch_transfers (
    transfer_id INT PRIMARY KEY IDENTITY(1,1),
    batch_id INT,
    from_user_id INT,
    to_pharmacy_id INT,
    transfer_date DATETIME DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'INITIATED',
    FOREIGN KEY (batch_id) REFERENCES medicine_batches(batch_id),
    FOREIGN KEY (from_user_id) REFERENCES users(user_id),
    FOREIGN KEY (to_pharmacy_id) REFERENCES pharmacies(pharmacy_id)
);

CREATE TABLE pharmacy_inventory (
    inventory_id INT PRIMARY KEY IDENTITY(1,1),
    pharmacy_id INT,
    batch_id INT,
    drug_id INT,
    quantity INT NOT NULL,
    added_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(pharmacy_id),
    FOREIGN KEY (batch_id) REFERENCES medicine_batches(batch_id),
    FOREIGN KEY (drug_id) REFERENCES drug_profiles(drug_id)
);

CREATE TABLE patient_profiles (
    profile_id INT PRIMARY KEY IDENTITY(1,1),
    patient_user_id INT,
    conditions TEXT,
    allergies TEXT,
    FOREIGN KEY (patient_user_id) REFERENCES users(user_id)
);

CREATE TABLE patient_medications (
    med_id INT PRIMARY KEY IDENTITY(1,1),
    patient_user_id INT,
    drug_id INT,
    dosage VARCHAR(50),
    frequency VARCHAR(50),
    is_active BIT DEFAULT 1,
    added_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (patient_user_id) REFERENCES users(user_id),
    FOREIGN KEY (drug_id) REFERENCES drug_profiles(drug_id)
);

CREATE TABLE drug_interactions (
    interaction_id INT PRIMARY KEY IDENTITY(1,1),
    drug_id_1 INT,
    drug_id_2 INT,
    severity VARCHAR(20),
    description TEXT,
    FOREIGN KEY (drug_id_1) REFERENCES drug_profiles(drug_id),
    FOREIGN KEY (drug_id_2) REFERENCES drug_profiles(drug_id)
);

CREATE TABLE food_log (
    log_id INT PRIMARY KEY IDENTITY(1,1),
    patient_user_id INT,
    food_name VARCHAR(100),
    food_category VARCHAR(50),
    nutritional_props TEXT,
    logged_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (patient_user_id) REFERENCES users(user_id)
);

CREATE TABLE dietary_alerts (
    alert_id INT PRIMARY KEY IDENTITY(1,1),
    patient_user_id INT,
    food_log_id INT,
    conflicting_med INT,
    severity VARCHAR(20),
    reason TEXT,
    acknowledged BIT DEFAULT 0,
    acknowledged_at DATETIME,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (patient_user_id) REFERENCES users(user_id),
    FOREIGN KEY (food_log_id) REFERENCES food_log(log_id),
    FOREIGN KEY (conflicting_med) REFERENCES drug_profiles(drug_id)
);

CREATE TABLE sales (
    sale_id INT PRIMARY KEY IDENTITY(1,1),
    pharmacy_id INT,
    patient_user_id INT,
    batch_id INT,
    sale_date DATETIME DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'COMPLETED',
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(pharmacy_id),
    FOREIGN KEY (patient_user_id) REFERENCES users(user_id),
    FOREIGN KEY (batch_id) REFERENCES medicine_batches(batch_id)
);

CREATE TABLE disposal_log (
    disposal_id INT PRIMARY KEY IDENTITY(1,1),
    batch_id INT,
    pharmacy_id INT,
    reason VARCHAR(50),
    disposed_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (batch_id) REFERENCES medicine_batches(batch_id),
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(pharmacy_id)
);
GO

-- 3. INSERT SEED DATA
-- Users
INSERT INTO users (username, password_hash, full_name, email, role) VALUES
('admin1', 'admin_pass', 'Dr. Sarah Admin', 'admin@aegisrx.com', 'ADMIN'),
('mfg_pharmax', 'mfg_pass', 'Ahmed Khan (PharmaX)', 'ahmed@pharmax.com', 'MANUFACTURER'),
('mfg_medicore', 'mfg_pass2', 'Zara Ali (MediCore)', 'zara@medicore.com', 'MANUFACTURER'),
('pharm_citycare', 'pha_pass', 'Usman Pharmacy Staff', 'usman@citycare.com', 'PHARMACY'),
('pharm_health', 'pha_pass2', 'Fatima Pharmacy Staff', 'fatima@healthplus.com', 'PHARMACY'),
('patient_ali', 'pat_pass', 'Ali Hassan', 'ali.hassan@email.com', 'PATIENT');

-- Pharmacies
INSERT INTO pharmacies (pharmacy_name, city, region, staff_user_id) VALUES
('CityCare Pharmacy', 'Islamabad', 'Federal', 4),
('HealthPlus Pharmacy', 'Lahore', 'Punjab', 5);

-- Drug Profiles
INSERT INTO drug_profiles (drug_name, category, description, manufacturer_id) VALUES
('Warfarin', 'Anticoagulant', 'Blood thinner', 2),
('Aspirin', 'NSAID', 'Pain reliever', 2),
('Metformin', 'Antidiabetic', 'Diabetes medication', 3);

-- Batches
INSERT INTO medicine_batches (gbi, drug_id, manufacturer_id, production_date, expiry_date, quantity, status, current_holder) VALUES
('GBI-2026-X1Y2', 1, 2, '2026-01-01', '2028-01-01', 5000, 'ACTIVE', 2),
('GBI-2026-A9B0', 3, 3, '2026-02-01', '2028-02-01', 2500, 'IN_STOCK', 4);

-- Interactions
INSERT INTO drug_interactions (drug_id_1, drug_id_2, severity, description) VALUES
(1, 2, 'HIGH', 'Aspirin increases bleeding risk with Warfarin');

GO

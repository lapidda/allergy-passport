-- ===========================================
-- Allergy Passport - Database Schema
-- ===========================================
-- This file is for reference only.
-- Hibernate will auto-create/update tables based on entities.
-- Run this manually only if you want to set up the schema without Hibernate.

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(8) UNIQUE NOT NULL,
    google_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    bio VARCHAR(500),
    profile_picture BYTEA,
    profile_picture_content_type VARCHAR(50),
    google_picture_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster public ID lookups
CREATE INDEX IF NOT EXISTS idx_users_public_id ON users(public_id);
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id);

-- User allergies table
CREATE TABLE IF NOT EXISTS user_allergies (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    allergy_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_allergies_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT uk_user_allergy_type 
        UNIQUE(user_id, allergy_type)
);

-- Index for faster user allergy lookups
CREATE INDEX IF NOT EXISTS idx_user_allergies_user_id ON user_allergies(user_id);

-- ===========================================
-- Sample Data (for testing)
-- ===========================================
-- Uncomment to insert test data

/*
INSERT INTO users (public_id, google_id, email, display_name, bio, created_at)
VALUES 
    ('abc12345', 'google-id-123', 'test@example.com', 'Test User', 'I have multiple food allergies.', CURRENT_TIMESTAMP);

INSERT INTO user_allergies (user_id, allergy_type, severity, notes, created_at)
VALUES 
    (1, 'PEANUTS', 'SEVERE', 'Strict avoidance required. Traces can cause reaction.', CURRENT_TIMESTAMP),
    (1, 'DAIRY', 'INTOLERANCE', 'Lactose intolerant. Small amounts okay.', CURRENT_TIMESTAMP),
    (1, 'GLUTEN', 'INTOLERANCE', 'Causes digestive issues.', CURRENT_TIMESTAMP);
*/

-- =====================================================
-- Allergen System Migration Script (FIXED)
-- Migrates from enum-based to database-driven allergens
-- =====================================================

-- Step 1: Clean up existing data (remove rows with NULL allergen_id)
-- This clears old enum-based allergies since they can't be migrated
DELETE FROM user_allergies WHERE allergen_id IS NULL;

-- Step 2: Drop old allergy_type column if it exists
ALTER TABLE user_allergies DROP COLUMN IF EXISTS allergy_type;

-- Step 3: Add foreign key constraint (without IF NOT EXISTS - not supported)
-- Drop first if exists to avoid errors on re-run
ALTER TABLE user_allergies DROP CONSTRAINT IF EXISTS fk_user_allergy_allergen;
ALTER TABLE user_allergies
    ADD CONSTRAINT fk_user_allergy_allergen
    FOREIGN KEY (allergen_id) REFERENCES allergens(id) ON DELETE CASCADE;

-- Step 4: Update unique constraint to use allergen_id instead of allergy_type
-- Drop old constraints if they exist
ALTER TABLE user_allergies DROP CONSTRAINT IF EXISTS uk_user_allergy_type;
ALTER TABLE user_allergies DROP CONSTRAINT IF EXISTS user_allergies_user_id_allergen_id_key;

-- Add new unique constraint
ALTER TABLE user_allergies
    ADD CONSTRAINT user_allergies_user_id_allergen_id_key
    UNIQUE (user_id, allergen_id);

-- Step 5: Make allergen_id NOT NULL
ALTER TABLE user_allergies ALTER COLUMN allergen_id SET NOT NULL;

-- =====================================================
-- Verify Migration
-- =====================================================
-- Check that allergen tables exist
SELECT 'Categories: ' || COUNT(*) FROM allergen_categories;
SELECT 'Allergens: ' || COUNT(*) FROM allergens;
SELECT 'User Allergies: ' || COUNT(*) FROM user_allergies;

-- =====================================================
-- Migration Complete
-- =====================================================
-- Next steps:
-- 1. Restart your Spring Boot application
-- 2. The DataInitializationService will populate allergen data
-- 3. Users will need to re-add their allergies
-- =====================================================

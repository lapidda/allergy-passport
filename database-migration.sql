-- =====================================================
-- Allergen System Migration Script
-- Migrates from enum-based to database-driven allergens
-- =====================================================

-- Step 1: Drop old allergy_type column from user_allergies
-- (This will remove existing user allergy data - backup first if needed!)
ALTER TABLE user_allergies DROP COLUMN IF EXISTS allergy_type;

-- Step 2: Add new allergen_id column
ALTER TABLE user_allergies ADD COLUMN IF NOT EXISTS allergen_id BIGINT;

-- Step 3: Add foreign key constraint
ALTER TABLE user_allergies
    ADD CONSTRAINT IF NOT EXISTS fk_user_allergy_allergen
    FOREIGN KEY (allergen_id) REFERENCES allergens(id) ON DELETE CASCADE;

-- Step 4: Update unique constraint to use allergen_id instead of allergy_type
ALTER TABLE user_allergies DROP CONSTRAINT IF EXISTS uk_user_allergy_type;
ALTER TABLE user_allergies DROP CONSTRAINT IF EXISTS user_allergies_user_id_allergen_id_key;
ALTER TABLE user_allergies
    ADD CONSTRAINT user_allergies_user_id_allergen_id_key
    UNIQUE (user_id, allergen_id);

-- Step 5: Make allergen_id NOT NULL (after data migration if needed)
ALTER TABLE user_allergies ALTER COLUMN allergen_id SET NOT NULL;

-- =====================================================
-- Migration Complete
-- =====================================================
-- Next steps:
-- 1. Restart your Spring Boot application
-- 2. The DataInitializationService will populate allergen data
-- 3. Users will need to re-add their allergies
-- =====================================================

-- =====================================================
-- Fix Severity Constraint
-- Updates the check constraint to allow new severity levels
-- =====================================================

-- Drop the old severity check constraint
ALTER TABLE user_allergies DROP CONSTRAINT IF EXISTS user_allergies_severity_check;

-- Add new constraint with all 4 severity levels
ALTER TABLE user_allergies
    ADD CONSTRAINT user_allergies_severity_check
    CHECK (severity::text = ANY (ARRAY[
        'INTOLERANCE_LOW'::character varying,
        'INTOLERANCE_MILD'::character varying,
        'INTOLERANCE_SEVERE'::character varying,
        'SEVERE'::character varying
    ]::text[]));

-- Clean up duplicate unique constraint
ALTER TABLE user_allergies DROP CONSTRAINT IF EXISTS uklpfcepd1quxrwrg1iso3rbtol;

-- Verify constraints
SELECT conname, contype, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'user_allergies'::regclass
ORDER BY conname;

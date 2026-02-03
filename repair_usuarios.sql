-- ============================================================================
-- REPAIR SCRIPT - Add missing columns to usuarios table
-- Generated: 2026-02-03
-- ============================================================================
-- This script adds the missing columns that were deleted by cleanup_duplicates.sql
-- ============================================================================

BEGIN;

-- Add missing columns to usuarios table
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS usu_nombre_completo VARCHAR(255) NOT NULL DEFAULT 'Usuario Temporal';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS usu_email VARCHAR(255) UNIQUE NOT NULL DEFAULT 'temp@example.com';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS usu_password VARCHAR(255) NOT NULL DEFAULT '$2a$10$defaulthash';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS usu_activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS usu_vetado BOOLEAN NOT NULL DEFAULT FALSE;

-- Remove default constraints (they were just for adding the columns safely)
ALTER TABLE usuarios ALTER COLUMN usu_nombre_completo DROP DEFAULT;
ALTER TABLE usuarios ALTER COLUMN usu_email DROP DEFAULT;
ALTER TABLE usuarios ALTER COLUMN usu_password DROP DEFAULT;

COMMIT;

-- Verification: Show all columns in usuarios table
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'usuarios' 
ORDER BY ordinal_position;

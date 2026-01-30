-- =============================================================================
-- V15: Remove outdated reason constraint
-- =============================================================================
-- Purpose: Drop CHECK constraint on reason column to allow descriptive text
-- Date: 2026-01-30
-- =============================================================================

-- Drop the old CHECK constraint that limited reason to enum values
ALTER TABLE friend_suggestions 
DROP CONSTRAINT chk_suggestions_reason;

-- =============================================================================
-- Notes:
-- - The old constraint required reason to be one of: 
--   'MUTUAL_FRIENDS', 'LOCATION', 'HOBBIES', 'OTHER'
-- - New implementation uses descriptive text like:
--   '2 bạn chung', 'Cùng sống tại Hà Nội', etc.
-- - This provides better UX by showing specific reasons to users
-- =============================================================================

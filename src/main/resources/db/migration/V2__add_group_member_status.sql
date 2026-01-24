-- Migration to add status column to group_members
ALTER TABLE group_members
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED';

-- Update existing memberships to 'ACCEPTED' just in case the default didn't apply to existing rows correctly
UPDATE group_members SET status = 'ACCEPTED' WHERE status IS NULL OR status = '';

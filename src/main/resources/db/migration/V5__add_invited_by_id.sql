-- Add invited_by_id column to track who initiated invitations
ALTER TABLE group_members 
ADD COLUMN invited_by_id INT NULL;

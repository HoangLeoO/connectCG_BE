-- Add pinning fields to posts table
ALTER TABLE posts ADD COLUMN is_pinned BOOLEAN DEFAULT FALSE;
ALTER TABLE posts ADD COLUMN pinned_at TIMESTAMP NULL;

-- Add AI score field to posts table
ALTER TABLE posts ADD COLUMN ai_score DOUBLE DEFAULT 0.0;

-- Add violation tracking fields to group_members table
ALTER TABLE group_members ADD COLUMN violation_count INT DEFAULT 0;
ALTER TABLE group_members ADD COLUMN last_violation_at TIMESTAMP NULL;

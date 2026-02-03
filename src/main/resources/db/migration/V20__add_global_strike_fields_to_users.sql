-- Add global strike fields to users table
ALTER TABLE users ADD COLUMN violation_count INT DEFAULT 0;
ALTER TABLE users ADD COLUMN last_violation_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN locked_until TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN permanent_locked BOOLEAN DEFAULT FALSE;

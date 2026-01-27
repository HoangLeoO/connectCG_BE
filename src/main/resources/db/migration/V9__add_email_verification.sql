-- 1. Add is_enabled column to users table
ALTER TABLE users ADD COLUMN is_enabled BOOLEAN DEFAULT FALSE;

-- Update existing users to be enabled (optional, but good for existing data)
UPDATE users SET is_enabled = TRUE;

-- 2. Create verification_tokens table
CREATE TABLE verification_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    expiry_date DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

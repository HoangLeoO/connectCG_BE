-- Add AI moderation fields to posts table
ALTER TABLE `posts` 
ADD COLUMN `ai_status` VARCHAR(20) DEFAULT 'NOT_CHECKED',
ADD COLUMN `ai_reason` TEXT,
ADD COLUMN `checked_at` DATETIME,
ADD COLUMN `approved_by_id` INT;

-- Add foreign key for approved_by_id
ALTER TABLE `posts`
ADD CONSTRAINT `fk_posts_approved_by`
FOREIGN KEY (`approved_by_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

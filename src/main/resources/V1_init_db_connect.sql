drop database if exists connectcg_be;
create database connectcg_be;
use connectcg_be;

-- =============================================================================
-- Database Schema for ConnectCG
-- Version: Final v5
-- Generated Date: 2026-01-22
-- Database: MySQL
-- Description: Schema definition including Tables, Constraints, Indexes, and Relations
-- =============================================================================

-- Disable foreign key checks temporarily to allow table creation in any order if needed, but we will try to follow dependency order.
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 1. Auth & Profile
-- =============================================================================

-- 1. USERS
CREATE TABLE `users` (
                         `id` INT AUTO_INCREMENT PRIMARY KEY,
                         `username` VARCHAR(50) NOT NULL,
                         `email` VARCHAR(100) NOT NULL,
                         `password_hash` VARCHAR(255) NOT NULL,
                         `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
                         `is_locked` BOOLEAN DEFAULT FALSE,
                         `is_deleted` BOOLEAN DEFAULT FALSE,
                         `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                         `last_login` DATETIME,

                         CONSTRAINT `uk_users_username` UNIQUE (`username`),
                         CONSTRAINT `uk_users_email` UNIQUE (`email`),
                         CONSTRAINT `chk_users_role` CHECK (`role` IN ('ADMIN', 'USER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. REFRESH_TOKENS
CREATE TABLE `refresh_tokens` (
                                  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  `user_id` INT NOT NULL,
                                  `token_hash` VARCHAR(255) NOT NULL,
                                  `user_agent` TEXT,
                                  `ip_address` VARCHAR(45),
                                  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  `last_used_at` DATETIME,
                                  `expires_at` DATETIME NOT NULL,
                                  `is_revoked` BOOLEAN DEFAULT FALSE,

                                  CONSTRAINT `uk_refresh_tokens_hash` UNIQUE (`token_hash`),
                                  CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. CITIES (Creating before Profiles)
CREATE TABLE `cities` (
                          `id` INT AUTO_INCREMENT PRIMARY KEY,
                          `code` VARCHAR(50) NOT NULL,
                          `name` VARCHAR(100) NOT NULL,
                          `region` VARCHAR(50),

                          CONSTRAINT `uk_cities_code` UNIQUE (`code`),
                          CONSTRAINT `chk_cities_region` CHECK (`region` IN ('NORTH', 'CENTRAL', 'SOUTH'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. USER_PROFILES
CREATE TABLE `user_profiles` (
                                 `id` INT AUTO_INCREMENT PRIMARY KEY,
                                 `user_id` INT NOT NULL,
                                 `city_id` INT,
                                 `full_name` VARCHAR(100),
                                 `date_of_birth` DATE,
                                 `gender` VARCHAR(20),
                                 `bio` VARCHAR(255),
                                 `occupation` VARCHAR(100),
                                 `marital_status` VARCHAR(20),
                                 `looking_for` VARCHAR(20),
                                 `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                 CONSTRAINT `uk_profiles_user_id` UNIQUE (`user_id`),
                                 CONSTRAINT `fk_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `fk_profiles_city` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`) ON DELETE SET NULL,
                                 CONSTRAINT `chk_profiles_gender` CHECK (`gender` IN ('MALE', 'FEMALE', 'OTHER')),
                                 CONSTRAINT `chk_profiles_marital_status` CHECK (`marital_status` IN ('SINGLE', 'DIVORCED', 'WIDOWED', 'MARRIED')),
                                 CONSTRAINT `chk_profiles_looking_for` CHECK (`looking_for` IN ('LOVE', 'FRIENDS', 'NETWORKING'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. HOBBIES
CREATE TABLE `hobbies` (
                           `id` INT AUTO_INCREMENT PRIMARY KEY,
                           `code` VARCHAR(50) NOT NULL,
                           `name` VARCHAR(100) NOT NULL,
                           `icon` VARCHAR(50),
                           `category` VARCHAR(50),

                           CONSTRAINT `uk_hobbies_code` UNIQUE (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. USER_HOBBIES
CREATE TABLE `user_hobbies` (
                                `user_id` INT NOT NULL,
                                `hobby_id` INT NOT NULL,

                                PRIMARY KEY (`user_id`, `hobby_id`),
                                CONSTRAINT `fk_user_hobbies_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                CONSTRAINT `fk_user_hobbies_hobby` FOREIGN KEY (`hobby_id`) REFERENCES `hobbies` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 2. Media System
-- =============================================================================

-- 7. MEDIA
CREATE TABLE `media` (
                         `id` INT AUTO_INCREMENT PRIMARY KEY,
                         `uploader_id` INT,
                         `url` VARCHAR(255) NOT NULL,
                         `thumbnail_url` VARCHAR(255),
                         `type` VARCHAR(20) NOT NULL,
                         `size_bytes` INT,
                         `uploaded_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                         `is_deleted` BOOLEAN DEFAULT FALSE,

                         CONSTRAINT `fk_media_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
                         CONSTRAINT `chk_media_type` CHECK (`type` IN ('IMAGE', 'VIDEO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. USER_AVATARS
CREATE TABLE `user_avatars` (
                                `id` INT AUTO_INCREMENT PRIMARY KEY,
                                `user_id` INT NOT NULL,
                                `media_id` INT NOT NULL,
                                `is_current` BOOLEAN DEFAULT FALSE,
                                `set_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT `fk_avatars_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                CONSTRAINT `fk_avatars_media` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. USER_COVERS
CREATE TABLE `user_covers` (
                               `id` INT AUTO_INCREMENT PRIMARY KEY,
                               `user_id` INT NOT NULL,
                               `media_id` INT NOT NULL,
                               `is_current` BOOLEAN DEFAULT FALSE,
                               `set_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT `fk_covers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_covers_media` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. USER_GALLERY
CREATE TABLE `user_gallery` (
                                `id` INT AUTO_INCREMENT PRIMARY KEY,
                                `user_id` INT NOT NULL,
                                `media_id` INT NOT NULL,
                                `display_order` INT DEFAULT 0,
                                `is_verified` BOOLEAN DEFAULT FALSE,
                                `added_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT `fk_gallery_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                CONSTRAINT `fk_gallery_media` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 3. Social Graph
-- =============================================================================

-- 11. FRIEND_REQUESTS
CREATE TABLE `friend_requests` (
                                   `id` INT AUTO_INCREMENT PRIMARY KEY,
                                   `sender_id` INT NOT NULL,
                                   `receiver_id` INT NOT NULL,
                                   `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                   `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   `responded_at` DATETIME,

                                   CONSTRAINT `fk_requests_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_requests_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                   CONSTRAINT `chk_requests_status` CHECK (`status` IN ('PENDING', 'ACCEPTED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. FRIENDS
CREATE TABLE `friends` (
                           `user_id` INT NOT NULL,
                           `friend_id` INT NOT NULL,
                           `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                           PRIMARY KEY (`user_id`, `friend_id`),
                           CONSTRAINT `fk_friends_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                           CONSTRAINT `fk_friends_friend` FOREIGN KEY (`friend_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- 14. FRIEND_SUGGESTIONS
CREATE TABLE `friend_suggestions` (
                                      `id` INT AUTO_INCREMENT PRIMARY KEY,
                                      `user_id` INT NOT NULL,
                                      `suggested_user_id` INT NOT NULL,
                                      `score` DECIMAL(5,2) DEFAULT 0,
                                      `reason` VARCHAR(50),
                                      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      `expires_at` DATETIME,

                                      CONSTRAINT `fk_suggestions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                      CONSTRAINT `fk_suggestions_target` FOREIGN KEY (`suggested_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                      CONSTRAINT `chk_suggestions_reason` CHECK (`reason` IN ('MUTUAL_FRIENDS', 'LOCATION', 'HOBBIES', 'OTHER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. DISMISSED_SUGGESTIONS
CREATE TABLE `dismissed_suggestions` (
                                         `user_id` INT NOT NULL,
                                         `dismissed_user_id` INT NOT NULL,
                                         `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                                         PRIMARY KEY (`user_id`, `dismissed_user_id`),
                                         CONSTRAINT `fk_dismissed_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                         CONSTRAINT `fk_dismissed_target` FOREIGN KEY (`dismissed_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 5. Community (Groups) - Moved up because Posts reference Groups
-- =============================================================================

-- 16. GROUPS (22 in Plan)
CREATE TABLE `groups` (
                          `id` INT AUTO_INCREMENT PRIMARY KEY,
                          `owner_id` INT NOT NULL,
                          `name` VARCHAR(100) NOT NULL,
                          `description` TEXT,
                          `cover_media_id` INT,
                          `privacy` VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
                          `is_deleted` BOOLEAN DEFAULT FALSE,
                          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT `fk_groups_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                          CONSTRAINT `fk_groups_cover` FOREIGN KEY (`cover_media_id`) REFERENCES `media` (`id`) ON DELETE SET NULL,
                          CONSTRAINT `chk_groups_privacy` CHECK (`privacy` IN ('PUBLIC', 'PRIVATE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. GROUP_MEMBERS (23 in Plan)
CREATE TABLE `group_members` (
                                 `group_id` INT NOT NULL,
                                 `user_id` INT NOT NULL,
                                 `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
                                 `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (`group_id`, `user_id`),
                                 CONSTRAINT `fk_group_members_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `fk_group_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `chk_group_members_role` CHECK (`role` IN ('MEMBER', 'MODERATOR', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 4. Content & Interaction
-- =============================================================================

-- 18. POSTS (16 in Plan)
CREATE TABLE `posts` (
                         `id` INT AUTO_INCREMENT PRIMARY KEY,
                         `author_id` INT NOT NULL,
                         `group_id` INT,
                         `content` TEXT,
                         `visibility` VARCHAR(20) DEFAULT 'PUBLIC',
                         `status` VARCHAR(20) DEFAULT 'APPROVED',
                         `is_deleted` BOOLEAN DEFAULT FALSE,
                         `comment_count` INT DEFAULT 0,
                         `react_count` INT DEFAULT 0,
                         `share_count` INT DEFAULT 0,
                         `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT `fk_posts_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                         CONSTRAINT `fk_posts_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE,
                         CONSTRAINT `chk_posts_visibility` CHECK (`visibility` IN ('PUBLIC', 'FRIENDS', 'PRIVATE')),
                         CONSTRAINT `chk_posts_status` CHECK (`status` IN ('APPROVED', 'PENDING', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. POST_MEDIA (17 in Plan)
CREATE TABLE `post_media` (
                              `post_id` INT NOT NULL,
                              `media_id` INT NOT NULL,
                              `display_order` INT DEFAULT 0,

                              PRIMARY KEY (`post_id`, `media_id`),
                              CONSTRAINT `fk_post_media_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
                              CONSTRAINT `fk_post_media_media` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- 22. COMMENTS (21 in Plan)
CREATE TABLE `comments` (
                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                            `post_id` INT NOT NULL,
                            `author_id` INT NOT NULL,
                            `parent_id` INT,
                            `content` TEXT,
                            `media_id` INT,
                            `is_deleted` BOOLEAN DEFAULT FALSE,
                            `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT `fk_comments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
                            CONSTRAINT `fk_comments_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                            CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
                            CONSTRAINT `fk_comments_media` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. REACTIONS (20 in Plan)
CREATE TABLE `reactions` (
                             `user_id` INT NOT NULL,
                             `post_id` INT NOT NULL,
                             `type` VARCHAR(20) NOT NULL,
                             `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                             PRIMARY KEY (`user_id`, `post_id`),
                             CONSTRAINT `fk_reactions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                             CONSTRAINT `fk_reactions_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
                             CONSTRAINT `chk_reactions_type` CHECK (`type` IN ('LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 6. Messaging
-- =============================================================================

-- 24. CHAT_ROOMS
CREATE TABLE `chat_rooms` (
                              `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                              `type` VARCHAR(20) NOT NULL,
                              `name` VARCHAR(100),
                              `avatar_url` VARCHAR(255),
                              `created_by` INT NOT NULL,
                              `firebase_room_key` VARCHAR(100) NOT NULL,
                              `last_message_at` DATETIME,
                              `is_active` BOOLEAN DEFAULT TRUE,
                              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT `uk_chat_rooms_firebase` UNIQUE (`firebase_room_key`),
                              CONSTRAINT `fk_chat_rooms_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                              CONSTRAINT `chk_chat_rooms_type` CHECK (`type` IN ('DIRECT', 'GROUP'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. CHAT_ROOM_MEMBERS
CREATE TABLE `chat_room_members` (
                                     `chat_room_id` BIGINT NOT NULL,
                                     `user_id` INT NOT NULL,
                                     `role` VARCHAR(20) DEFAULT 'MEMBER',
                                     `nickname` VARCHAR(100),
                                     `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     `left_at` DATETIME,

                                     PRIMARY KEY (`chat_room_id`, `user_id`),
                                     CONSTRAINT `fk_chat_members_room` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE CASCADE,
                                     CONSTRAINT `fk_chat_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                     CONSTRAINT `chk_chat_members_role` CHECK (`role` IN ('MEMBER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- =============================================================================
-- =============================================================================
-- 7. System
-- =============================================================================

-- 28. NOTIFICATIONS
CREATE TABLE `notifications` (
                                 `id` INT AUTO_INCREMENT PRIMARY KEY,
                                 `user_id` INT NOT NULL,
                                 `actor_id` INT,
                                 `type` VARCHAR(50) NOT NULL,
                                 `target_type` VARCHAR(50) NOT NULL,
                                 `target_id` INT NOT NULL,
                                 `is_read` BOOLEAN DEFAULT FALSE,
                                 `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `fk_notifications_actor` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `chk_notifications_type` CHECK (`type` IN ('LIKE', 'COMMENT', 'FRIEND_REQUEST', 'GROUP_INVITE', 'MESSAGE', 'OTHER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 29. REPORTS
CREATE TABLE `reports` (
                           `id` INT AUTO_INCREMENT PRIMARY KEY,
                           `reporter_id` INT NOT NULL,
                           `target_type` VARCHAR(50) NOT NULL,
                           `target_id` INT NOT NULL,
                           `reason` VARCHAR(255),
                           `status` VARCHAR(20) DEFAULT 'PENDING',
                           `reviewer_id` INT,
                           `admin_note` TEXT,
                           `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                           `resolved_at` DATETIME,

                           CONSTRAINT `fk_reports_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                           CONSTRAINT `fk_reports_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
                           CONSTRAINT `chk_reports_target_type` CHECK (`target_type` IN ('USER', 'POST', 'COMMENT', 'GROUP', 'MESSAGE')),
                           CONSTRAINT `chk_reports_status` CHECK (`status` IN ('PENDING', 'REVIEWING', 'RESOLVED', 'DISMISSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- Indexes for Optimization
-- =============================================================================

-- Media
CREATE INDEX `idx_media_uploader_type` ON `media`(`uploader_id`, `type`);

-- User Gallery
CREATE INDEX `idx_user_gallery_order` ON `user_gallery`(`user_id`, `display_order`);

-- Posts
CREATE INDEX `idx_posts_created_at` ON `posts`(`created_at` DESC);
CREATE INDEX `idx_posts_status` ON `posts`(`status`);

-- Chat
CREATE UNIQUE INDEX `idx_chat_firebase_key` ON `chat_rooms`(`firebase_room_key`);
CREATE INDEX `idx_chat_last_message` ON `chat_rooms`(`last_message_at` DESC);



-- Notifications
CREATE INDEX `idx_notifications_user_read` ON `notifications`(`user_id`, `is_read`);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- DEFERRED FEATURES (Commented out)
-- =============================================================================

/*
-- 13. BLOCKED_USERS
CREATE TABLE `blocked_users` (
    `user_id` INT NOT NULL,
    `blocked_user_id` INT NOT NULL,
    `reason` VARCHAR(255),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`user_id`, `blocked_user_id`),
    CONSTRAINT `fk_blocked_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_blocked_target` FOREIGN KEY (`blocked_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. HASHTAGS (18 in Plan)
CREATE TABLE `hashtags` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `post_count` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT `uk_hashtags_name` UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. POST_HASHTAGS (19 in Plan)
CREATE TABLE `post_hashtags` (
    `post_id` INT NOT NULL,
    `hashtag_id` INT NOT NULL,

    PRIMARY KEY (`post_id`, `hashtag_id`),
    CONSTRAINT `fk_post_hashtags_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_hashtags_tag` FOREIGN KEY (`hashtag_id`) REFERENCES `hashtags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
*/

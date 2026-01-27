-- =============================================================================
-- Sample Data for Testing Group Invitations
-- =============================================================================


-- Insert sample cities (ignore if already exists)
INSERT IGNORE INTO `cities` (`code`, `name`, `region`) VALUES
('HN', 'Hà Nội', 'NORTH'),
('HCM', 'Hồ Chí Minh', 'SOUTH'),
('DN', 'Đà Nẵng', 'CENTRAL');

-- Insert sample users (password: password123)
INSERT IGNORE INTO `users` (`username`, `email`, `password_hash`, `role`) VALUES
('john_doe', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER'),
('jane_smith', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER'),
('bob_wilson', 'bob@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER'),
('alice_brown', 'alice@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER'),
('charlie_davis', 'charlie@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER');

-- Insert user profiles
INSERT IGNORE INTO `user_profiles` (`user_id`, `city_id`, `full_name`, `date_of_birth`, `gender`, `bio`, `occupation`) VALUES
(3, 1, 'John Doe', '1995-05-15', 'MALE', 'Software Developer from Hanoi', 'Software Engineer'),
(4, 2, 'Jane Smith', '1997-08-22', 'FEMALE', 'Designer and coffee lover', 'UI/UX Designer'),
(5, 3, 'Bob Wilson', '1993-03-10', 'MALE', 'Photographer and traveler', 'Photographer'),
(6, 1, 'Alice Brown', '1996-11-30', 'FEMALE', 'Marketing professional', 'Marketing Manager'),
(7, 2, 'Charlie Davis', '1994-07-18', 'MALE', 'Tech enthusiast', 'Data Analyst');

-- Insert media for avatars
INSERT IGNORE INTO `media` (`id`, `uploader_id`, `url`, `type`) VALUES
(100, 1, 'https://i.pravatar.cc/150?img=12', 'IMAGE'),
(101, 2, 'https://i.pravatar.cc/150?img=5', 'IMAGE'),
(102, 3, 'https://i.pravatar.cc/150?img=33', 'IMAGE'),
(103, 4, 'https://i.pravatar.cc/150?img=9', 'IMAGE'),
(104, 5, 'https://i.pravatar.cc/150?img=68', 'IMAGE');

-- Insert user avatars
INSERT IGNORE INTO `user_avatars` (`user_id`, `media_id`, `is_current`) VALUES
(3, 100, TRUE),
(4, 101, TRUE),
(5, 102, TRUE),
(6, 103, TRUE),
(7, 104, TRUE);

-- Insert friend relationships (bidirectional)
-- User 1 (john_doe) is friends with users 2, 3, 4, 5
INSERT IGNORE INTO `friends` (`user_id`, `friend_id`) VALUES
(2, 3), (3, 2),
(2, 4), (4, 2),
(2, 5), (5, 2),
(2, 6), (6, 2);


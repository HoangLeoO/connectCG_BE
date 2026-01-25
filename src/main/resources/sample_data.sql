-- SAMPLE DATA FOR CONNECTCG_BE
-- Database: connectcg_be

SET FOREIGN_KEY_CHECKS = 0;

-- 1. CITIES
INSERT INTO `cities` (`id`, `code`, `name`, `region`) VALUES
(1, 'HANOI', 'Hà Nội', 'NORTH'),
(2, 'SAIGON', 'TP. Hồ Chí Minh', 'SOUTH'),
(3, 'DANANG', 'Đà Nẵng', 'CENTRAL');

-- 2. USERS
-- Password hash for 'password123' (BCrypt)
-- $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmG5Xe5A.8mC
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `role`) VALUES
(1, 'admin', 'admin@connectcg.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmG5Xe5A.8mC', 'ADMIN'),
(2, 'tung_nguyen', 'tung@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmG5Xe5A.8mC', 'USER'),
(3, 'lan_anh', 'lananh@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmG5Xe5A.8mC', 'USER'),
(4, 'stranger_user', 'stranger@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmG5Xe5A.8mC', 'USER');

-- 3. HOBBIES
INSERT INTO `hobbies` (`id`, `code`, `name`, `icon`, `category`) VALUES
(1, 'MUSIC', 'Âm nhạc', 'music_icon', 'ENTERTAINMENT'),
(2, 'SPORTS', 'Thể thao', 'sports_icon', 'HEALTH'),
(3, 'COFFEE', 'Cà phê', 'coffee_icon', 'SOCIAL'),
(4, 'CODING', 'Lập trình', 'code_icon', 'WORK');

-- 4. USER_PROFILES
INSERT INTO `user_profiles` (`user_id`, `city_id`, `full_name`, `date_of_birth`, `gender`, `bio`, `occupation`, `marital_status`, `looking_for`) VALUES
(1, 1, 'Hệ thống Admin', '1990-01-01', 'MALE', 'Quản trị viên hệ thống ConnectCG', 'Administrator', 'MARRIED', 'NETWORKING'),
(2, 1, 'Nguyễn Thanh Tùng', '1995-05-20', 'MALE', 'Thích lập trình và đi phượt', 'Software Engineer', 'SINGLE', 'FRIENDS'),
(3, 2, 'Lê Lan Anh', '1998-10-15', 'FEMALE', 'Yêu âm nhạc và thích kết nối', 'Designer', 'SINGLE', 'LOVE'),
(4, 3, 'Người Lạ Ơi', '2000-12-12', 'OTHER', 'Chào mọi người, mình là người mới', 'Student', 'SINGLE', 'NETWORKING');

-- 5. USER_HOBBIES
INSERT INTO `user_hobbies` (`user_id`, `hobby_id`) VALUES
(2, 2), (2, 4), -- Tùng: Thể thao, Lập trình
(3, 1), (3, 3), -- Lan Anh: Âm nhạc, Cà phê
(4, 3);         -- Người lạ: Cà phê

-- 6. MEDIA
INSERT INTO `media` (`id`, `uploader_id`, `url`, `thumbnail_url`, `type`, `size_bytes`) VALUES
(1, 2, 'https://res.cloudinary.com/demo/image/upload/v1/tung_avatar.jpg', NULL, 'IMAGE', 102400),
(2, 2, 'https://res.cloudinary.com/demo/image/upload/v1/tung_cover.jpg', NULL, 'IMAGE', 204800),
(3, 3, 'https://res.cloudinary.com/demo/image/upload/v1/lan_avatar.jpg', NULL, 'IMAGE', 152400),
(4, 3, 'https://res.cloudinary.com/demo/image/upload/v1/lan_cover.jpg', NULL, 'IMAGE', 352400),
(5, 2, 'https://res.cloudinary.com/demo/image/upload/v1/gallery_1.jpg', NULL, 'IMAGE', 502400);

-- 7. USER_AVATARS
INSERT INTO `user_avatars` (`user_id`, `media_id`, `is_current`) VALUES
(2, 1, TRUE),
(3, 3, TRUE);

-- 8. USER_COVERS
INSERT INTO `user_covers` (`user_id`, `media_id`, `is_current`) VALUES
(2, 2, TRUE),
(3, 4, TRUE);

-- 9. USER_GALLERY
INSERT INTO `user_gallery` (`user_id`, `media_id`, `display_order`) VALUES
(2, 5, 1);

-- 10. FRIENDS (Tùng và Lan Anh là bạn bè)
INSERT INTO `friends` (`user_id`, `friend_id`) VALUES
(2, 3), (3, 2);

-- 11. GROUPS
INSERT INTO `groups` (`id`, `owner_id`, `name`, `description`, `privacy`) VALUES
(1, 1, 'Hội Lập Trình Viên Hà Nội', 'Nơi giao lưu của các dev tại Hà Nội', 'PUBLIC');

-- 12. GROUP_MEMBERS
INSERT INTO `group_members` (`group_id`, `user_id`, `role`) VALUES
(1, 1, 'ADMIN'),
(1, 2, 'MEMBER');

-- 13. POSTS
INSERT INTO `posts` (`id`, `author_id`, `content`, `visibility`, `status`) VALUES
(1, 2, 'Chào mừng các bạn đến với ConnectCG!', 'PUBLIC', 'APPROVED'),
(2, 3, 'Hôm nay trời thật là đẹp!', 'FRIENDS', 'APPROVED');

-- 14. COMMENTS
INSERT INTO `comments` (`id`, `post_id`, `author_id`, `content`) VALUES
(1, 1, 3, 'Chào anh Tùng nhé!'),
(2, 1, 2, 'Chào Lan Anh!');

-- 15. REACTIONS
INSERT INTO `reactions` (`user_id`, `post_id`, `type`) VALUES
(3, 1, 'LOVE'),
(2, 1, 'LIKE');

SET FOREIGN_KEY_CHECKS = 1;

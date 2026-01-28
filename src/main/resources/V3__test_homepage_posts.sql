-- =============================================================================
-- V11: Insert Test Homepage Posts for Gemini Moderation
-- These posts have group_id = NULL
-- =============================================================================

INSERT INTO `posts` (`author_id`, `group_id`, `content`, `visibility`, `status`, `is_deleted`, `created_at`) VALUES
-- 1. SAFE POSTS
(2, NULL, 'Chào buổi sáng cả nhà! Hôm nay thời tiết ở Hà Nội thật tuyệt vời, có ai đi cafe không?', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 5 HOUR),
(2, NULL, 'Mọi người cho mình hỏi có nguồn tài liệu nào học React Native tốt cho người mới bắt đầu không?', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 4 HOUR),
(3, NULL, 'Vừa hoàn thành chứng chỉ AWS Cloud Practitioner xong, vui quá! 🚀 #AWS #Cloud #Learning', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 3 HOUR),
(4, NULL, 'Có ai biết cách fix lỗi 403 Forbidden trong Spring Security khi dùng JWT không? Giúp mình với!', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 2 HOUR),

-- 2. TOXIC / INAPPROPRIATE POSTS (For AI Detection Testing)
(2, NULL, 'Mấy thằng admin trang này làm ăn như c** ấy, khóa acc tao vô lý!', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 90 MINUTE),
(3, NULL, 'Đm cuộc đời, chán vcl ra, có ai muốn đi nhậu không?', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 80 MINUTE),
(5, NULL, 'Thằng X là đồ ng* xuẩn, đừng có tin lời nó nói.', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 70 MINUTE),
(2, NULL, 'Cút ngay đi thằng rác rưởi, mày không xứng đáng ở đây.', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 60 MINUTE),

-- 3. BORDERLINE / SPAM
(3, NULL, 'CLICK VÀO ĐÂY ĐỂ NHẬN 1000$ MIỄN PHÍ NGAY HÔM NAY!!! LINK: http://lừa-đảo.com', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 50 MINUTE),
(5, NULL, 'Tối nay 8h có kèo bóng đá cực thơm, anh em vào nhóm Zalo soi kèo nhé!!!', 'PUBLIC', 'APPROVED', FALSE, NOW() - INTERVAL 40 MINUTE);

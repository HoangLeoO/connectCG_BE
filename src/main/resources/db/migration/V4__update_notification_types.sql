-- Update notification type constraint to support group requests
ALTER TABLE notifications DROP CONSTRAINT chk_notifications_type;

ALTER TABLE notifications 
ADD CONSTRAINT chk_notifications_type 
CHECK (type IN ('LIKE', 'COMMENT', 'FRIEND_REQUEST', 'GROUP_INVITE', 'GROUP_JOIN_REQUEST', 'GROUP_JOIN_APPROVED', 'MESSAGE', 'OTHER'));

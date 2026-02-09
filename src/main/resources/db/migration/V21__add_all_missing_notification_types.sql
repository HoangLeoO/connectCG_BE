-- V21: Add all missing notification types from codebase
-- This migration consolidates all notification types used in the application

ALTER TABLE notifications DROP CONSTRAINT chk_notifications_type;

ALTER TABLE notifications 
ADD CONSTRAINT chk_notifications_type 
CHECK (type IN (
  -- Friend & User
  'FRIEND_REQUEST', 'FRIEND_ACCEPT', 'ROLE_CHANGE',
  
  -- Post & Content (Comment, Reaction)
  'POST_COMMENT', 'COMMENT_REPLY', 'POST_REACTION',
  'POST_APPROVED', 'POST_REJECTED', 'POST_PENDING',
  
  -- Group Management
  'GROUP_INVITE', 'GROUP_INVITE_ACCEPTED',
  'GROUP_JOIN_REQUEST', 'GROUP_JOIN_APPROVED', 'GROUP_JOIN_REJECTED',
  'GROUP_MEMBER_JOINED', 'GROUP_MEMBER_LEFT',
  'GROUP_BANNED', 'GROUP_UNBAN',
  'GROUP_DELETED', 'GROUP_OWNER_CHANGE', 'GROUP_ROLE_CHANGED',
  
  -- Report
  'REPORT_SUBMITTED', 'REPORT_UPDATED',
  
  -- Legacy & Other
  'LIKE', 'COMMENT', 'GROUP_KICK', 'GROUP_REJECTED',
  'MESSAGE', 'OTHER'
));

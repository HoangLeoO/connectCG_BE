-- =============================================================================
-- V14: Improve Friend Suggestions System
-- =============================================================================
-- Purpose: Add constraints and indexes for better performance and data integrity
-- Date: 2026-01-30
-- =============================================================================

-- 1. Add UNIQUE constraint to prevent duplicate suggestions
-- This ensures each user can only have one suggestion for a specific user
ALTER TABLE friend_suggestions 
ADD CONSTRAINT uk_suggestions_pair UNIQUE (user_id, suggested_user_id);

-- 2. Add indexes for query performance
-- Index for fetching active suggestions by user
CREATE INDEX idx_suggestions_user_expires 
ON friend_suggestions(user_id, expires_at);

-- Index for sorting by score
CREATE INDEX idx_suggestions_score 
ON friend_suggestions(user_id, score DESC);

-- Index for dismissed suggestions lookup
CREATE INDEX idx_dismissed_user 
ON dismissed_suggestions(user_id);

-- Index for friends lookup (improve mutual friends query)
CREATE INDEX idx_friends_lookup 
ON friends(user_id, friend_id);

-- Index for friend requests status lookup
CREATE INDEX idx_friend_requests_status 
ON friend_requests(receiver_id, status);

CREATE INDEX idx_friend_requests_sender_status 
ON friend_requests(sender_id, status);

-- =============================================================================
-- Notes:
-- - uk_suggestions_pair prevents duplicate suggestions
-- - Indexes improve query performance for:
--   * Fetching suggestions by user
--   * Filtering expired suggestions
--   * Sorting by score
--   * Checking dismissed users
--   * Finding mutual friends
--   * Filtering pending requests
-- =============================================================================

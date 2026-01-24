-- Migration to add content column to notifications
ALTER TABLE notifications
ADD COLUMN content TEXT;

DROP PROCEDURE IF EXISTS upgrade_database_v17;

DELIMITER $$
CREATE PROCEDURE upgrade_database_v17()
BEGIN
    IF NOT EXISTS(
        SELECT *
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'chat_room_members'
        AND COLUMN_NAME = 'client_cleared_at'
    ) THEN
        ALTER TABLE chat_room_members ADD COLUMN client_cleared_at DATETIME DEFAULT NULL;
    END IF;
END $$
DELIMITER ;

CALL upgrade_database_v17();
DROP PROCEDURE upgrade_database_v17;

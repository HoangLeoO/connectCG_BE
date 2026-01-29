-- 1. Add new columns to user_profiles
ALTER TABLE user_profiles ADD COLUMN city_code VARCHAR(50);
ALTER TABLE user_profiles ADD COLUMN city_name VARCHAR(100);

-- 2. Drop foreign key constraint
ALTER TABLE user_profiles DROP FOREIGN KEY fk_profiles_city;

-- 3. Drop city_id column index if it exists (usually removed with column, but good to be safe if separate index exists)
-- DROP INDEX fk_profiles_city ON user_profiles; -- Index usually named same as FK in simplified creation, or auto-created.
-- MySQL automatically drops the index if it was created for the FK when the column is dropped, usually.
-- But safer to just drop column.

ALTER TABLE user_profiles DROP COLUMN city_id;

-- 4. Drop cities table
DROP TABLE IF EXISTS cities;

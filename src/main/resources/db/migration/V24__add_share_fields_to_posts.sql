ALTER TABLE posts
    ADD COLUMN original_post_id INTEGER DEFAULT NULL,
    ADD CONSTRAINT fk_posts_original_post FOREIGN KEY (original_post_id) REFERENCES posts (id) ON DELETE SET NULL;
package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.PostMedia;
import org.example.connectcg_be.entity.PostMediaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, PostMediaId> {
}

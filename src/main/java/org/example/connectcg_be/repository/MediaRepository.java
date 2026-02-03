package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Integer> {
    List<Media> findAllByUploaderIdAndIsDeletedFalseOrderByUploadedAtDesc(Integer uploaderId);
}

package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.UserGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGalleryRepository extends JpaRepository<UserGallery, Integer> {
}

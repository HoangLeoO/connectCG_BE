package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.UserHobby;
import org.example.connectcg_be.entity.UserHobbyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHobbyRepository extends JpaRepository<UserHobby, UserHobbyId> {
}

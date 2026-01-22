package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Friend;
import org.example.connectcg_be.entity.FriendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendId> {
}

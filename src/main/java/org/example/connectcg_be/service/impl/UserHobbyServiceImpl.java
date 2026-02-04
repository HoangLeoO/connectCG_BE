package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.entity.Hobby;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.entity.UserHobby;
import org.example.connectcg_be.entity.UserHobbyId;
import org.example.connectcg_be.repository.HobbyRepository;
import org.example.connectcg_be.repository.UserHobbyRepository;
import org.example.connectcg_be.repository.UserRepository;
import org.example.connectcg_be.service.UserHobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserHobbyServiceImpl implements UserHobbyService {

    @Autowired
    private UserHobbyRepository userHobbyRepository;

    @Autowired
    private HobbyRepository hobbyRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void updateUserHobbies(Integer userId, List<Integer> hobbyIds) {
        // 1. Validate User
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        // 2. Delete existing hobbies for this user
        userHobbyRepository.deleteByUserId(userId);

        // 3. Insert new hobbies
        if (hobbyIds != null && !hobbyIds.isEmpty()) {
            List<Hobby> hobbies = hobbyRepository.findAllById(hobbyIds);

            // Needed for @MapsId
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

            for (Hobby hobby : hobbies) {
                UserHobby userHobby = new UserHobby();
                UserHobbyId id = new UserHobbyId(userId, hobby.getId());
                userHobby.setId(id);

                // Must set relationships because of @MapsId
                userHobby.setUser(user);
                userHobby.setHobby(hobby);

                userHobbyRepository.save(userHobby);
            }
        }
    }
}

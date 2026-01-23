package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.UserRepository;
import org.example.connectcg_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findByIdUser(int id) {
        return userRepository.findById(id).orElse(null);
    }
}

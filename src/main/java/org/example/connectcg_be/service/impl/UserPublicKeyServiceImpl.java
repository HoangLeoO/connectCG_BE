package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.entity.UserPublicKey;
import org.example.connectcg_be.repository.UserPublicKeyRepository;
import org.example.connectcg_be.service.UserPublicKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class UserPublicKeyServiceImpl implements UserPublicKeyService {

    @Autowired
    private UserPublicKeyRepository repository;

    @Override
    @Transactional
    public void savePublicKey(User user, String publicKey) {
        UserPublicKey userKey = repository.findById(user.getId())
                .orElse(new UserPublicKey());

        userKey.setUserId(user.getId());
        userKey.setUser(user);
        userKey.setPublicKey(publicKey);
        userKey.setCreatedAt(Instant.now());

        repository.save(userKey);
    }

    @Override
    public String getPublicKey(Integer userId) {
        return repository.findById(userId)
                .map(UserPublicKey::getPublicKey)
                .orElse(null);
    }
}

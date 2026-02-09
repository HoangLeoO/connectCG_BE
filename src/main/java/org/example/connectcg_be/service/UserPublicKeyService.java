package org.example.connectcg_be.service;

import org.example.connectcg_be.entity.User;

public interface UserPublicKeyService {
    void savePublicKey(User user, String publicKey);

    String getPublicKey(Integer userId);
}

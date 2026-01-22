package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.RefreshTokenRepository;
import org.example.connectcg_be.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
}

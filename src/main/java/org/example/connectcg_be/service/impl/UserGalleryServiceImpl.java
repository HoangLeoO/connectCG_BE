package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.UserGalleryRepository;
import org.example.connectcg_be.service.UserGalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserGalleryServiceImpl implements UserGalleryService {

    @Autowired
    private UserGalleryRepository userGalleryRepository;
}

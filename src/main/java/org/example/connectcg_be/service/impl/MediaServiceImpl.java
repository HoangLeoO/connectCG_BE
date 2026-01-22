package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.MediaRepository;
import org.example.connectcg_be.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private MediaRepository mediaRepository;
}

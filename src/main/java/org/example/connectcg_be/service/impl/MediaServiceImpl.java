package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.entity.Media;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.MediaRepository;
import org.example.connectcg_be.service.MediaService;
import org.example.connectcg_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private UserService userService;

    @Override
    public Media createCoverMedia(String url, int userId) {
        User user = userService.findByIdUser(userId);
        Media media = new Media();
        media.setUploader(user);
        media.setUrl(url);
        media.setType("image");
        media.setUploadedAt(Instant.now());
        media.setIsDeleted(false);
        return mediaRepository.save(media);
    }
}
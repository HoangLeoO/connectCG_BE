package org.example.connectcg_be.service;

import org.example.connectcg_be.entity.Media;

public interface MediaService {
    Media createCoverMedia(String url, int userId);
}
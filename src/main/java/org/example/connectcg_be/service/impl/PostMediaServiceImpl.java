package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.PostMediaRepository;
import org.example.connectcg_be.service.PostMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostMediaServiceImpl implements PostMediaService {

    @Autowired
    private PostMediaRepository postMediaRepository;
}

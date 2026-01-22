package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.CommentRepository;
import org.example.connectcg_be.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
}

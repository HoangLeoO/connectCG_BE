package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.HobbyDTO;
import org.example.connectcg_be.repository.HobbyRepository;
import org.example.connectcg_be.service.HobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HobbyServiceImpl implements HobbyService {

    @Autowired
    private HobbyRepository hobbyRepository;

    @Override
    public List<HobbyDTO> getAllHobbies() {
        return hobbyRepository.findAll().stream()
                .map(hobby ->HobbyDTO.builder()
                        .id(hobby.getId())
                        .code(hobby.getCode())
                        .name(hobby.getName())
                        .icon(hobby.getIcon())
                        .category(hobby.getCategory())
                        .build())
                .collect(Collectors.toList());
    }
}

package org.example.connectcg_be.controller;

import org.example.connectcg_be.dto.HobbyDTO;
import org.example.connectcg_be.service.HobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hobbies")
public class HobbyController {

    @Autowired
    private HobbyService hobbyService;

    @GetMapping
    public ResponseEntity<List<HobbyDTO>> getAllHobbies() {
        return ResponseEntity.ok(hobbyService.getAllHobbies());
    }
}

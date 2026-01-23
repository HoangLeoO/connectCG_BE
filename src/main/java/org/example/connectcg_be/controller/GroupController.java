package org.example.connectcg_be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.service.GroupMemberService;
import org.example.connectcg_be.service.GroupService;
import org.example.connectcg_be.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin("*")
public class GroupController {
    @Autowired
    GroupService groupService;
    @Autowired
    MediaService mediaService;
    @Autowired
    GroupMemberService groupMemberService;

    @GetMapping
    public List<GroupDTO> getAll() {
        return groupService.findAllGroups();
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateGroup request) {
        groupService.addGroup(request, 1);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}

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
import org.example.connectcg_be.security.UserPrincipal;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<Void> create(@Valid @RequestBody CreateGroup request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.addGroup(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/my-groups")
    public List<GroupDTO> getMyGroups(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findMyGroups(userPrincipal.getId());
    }

    @GetMapping("/discover")
    public List<GroupDTO> getDiscoverGroups(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findDiscoverGroups(userPrincipal.getId());
    }

    @GetMapping("/search")
    public List<GroupDTO> searchGroups(@RequestParam("name") String name) {
        return groupService.searchGroups(name);
    }

    @GetMapping("/{id}")
    public GroupDTO getById(@PathVariable("id") Integer id) {
        return groupService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Integer id, @Valid @RequestBody CreateGroup request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.updateGroup(id, request, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

}

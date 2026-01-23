package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.GroupMemberRepository;
import org.example.connectcg_be.repository.GroupRepository;
import org.example.connectcg_be.service.GroupService;
import org.example.connectcg_be.service.MediaService;
import org.example.connectcg_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MediaService mediaService;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public List<GroupDTO> findAllGroups() {
        return groupRepository.findAll().stream().map(group -> {
            String ownerName = group.getOwner() != null ? group.getOwner().getUsername() : null;
            Integer ownerId = group.getOwner() != null ? group.getOwner().getId() : null;
            String imageUrl = group.getCoverMedia() != null ? group.getCoverMedia().getUrl() : null;
            Integer coverMediaId = group.getCoverMedia() != null ? group.getCoverMedia().getId() : null;

            return new GroupDTO(
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    group.getPrivacy(),
                    group.getIsDeleted(),
                    group.getCreatedAt(),
                    ownerId,
                    ownerName,
                    coverMediaId,
                    imageUrl);
        }).collect(Collectors.toList());
    }

    @Transactional
    public Group addGroup(CreateGroup request, int userId) {

        User owner = userService.findByIdUser(userId);

        Media media = null;
        if (request.getImage() != null) {
            media = mediaService.createCoverMedia(request.getImage(), userId);
        }

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setPrivacy(request.getPrivacy());
        group.setOwner(owner);
        group.setCoverMedia(media);
        group.setCreatedAt(Instant.now());
        group.setIsDeleted(false);

        Group savedGroup = groupRepository.save(group);

        GroupMemberId memberId = new GroupMemberId();
        memberId.setGroupId(savedGroup.getId());
        memberId.setUserId(owner.getId());

        GroupMember member = new GroupMember();
        member.setId(memberId);
        member.setGroup(savedGroup);
        member.setUser(owner);
        member.setRole("admin");
        member.setJoinedAt(Instant.now());

        groupMemberRepository.save(member);

        return savedGroup;
    }

}

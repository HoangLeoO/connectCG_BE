package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.entity.Group;

import java.util.List;

public interface GroupService {
    List<GroupDTO> findAllGroups();

    Group addGroup(CreateGroup request, int userId);
}

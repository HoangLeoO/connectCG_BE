package org.example.connectcg_be.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class GroupDTO {
    private Integer id;
    private String name;
    private String description;
    private String privacy;
    private Boolean isDeleted;
    private Instant createdAt;

    // Owner Info
    private Integer ownerId;
    private String ownerName; // username

    // Cover Info
    private Integer coverMediaId;
    private String image; // URL

    public GroupDTO(Integer id, String name, String description, String privacy, Boolean isDeleted, Instant createdAt,
            Integer ownerId, String ownerName, Integer coverMediaId, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.privacy = privacy;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.coverMediaId = coverMediaId;
        this.image = image;
    }
}

package org.example.connectcg_be.dto;

import lombok.Data;

@Data
public class MediaItem {
    private String url;
    private String type;       // image/video
    private Integer displayOrder;
}

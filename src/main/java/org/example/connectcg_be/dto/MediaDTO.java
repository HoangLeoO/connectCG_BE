package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDTO {
    private Integer id;
    private String url;
    private String thumbnailUrl;
    private String type;
    private Integer sizeBytes;
    private Instant uploadedAt;
}

package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HobbyDTO {
    private Integer id;
    private String code;
    private String name;
    private String icon;
    private String category;
}

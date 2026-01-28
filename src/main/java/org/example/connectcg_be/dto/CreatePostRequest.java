package org.example.connectcg_be.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @Size(max = 20)
    private String visibility = "PUBLIC"; // PUBLIC, FRIENDS, PRIVATE

    private Integer groupId; // null for homepage posts
}

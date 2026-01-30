package org.example.connectcg_be.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 5000, message = "Nội dung tối đa 5000 kí tự")
    private String content;
    @Size(max = 20)
    @Pattern(
            regexp = "PUBLIC|FRIENDS|PRIVATE",
            message = "visibility chỉ được là PUBLIC, FRIENDS hoặc PRIVATE"
    )
    private String visibility = "PUBLIC"; // PUBLIC, FRIENDS, PRIVATE
    private Integer groupId; // null for homepage posts
    @Size(max = 10, message = "Bạn chỉ có thể đính kèm tối đa 10 media")
    private List<@NotBlank(message = "Media URL không được để trống") String> mediaUrls;
}

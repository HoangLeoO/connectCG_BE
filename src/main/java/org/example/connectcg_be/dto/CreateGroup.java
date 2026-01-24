package org.example.connectcg_be.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGroup {
    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(min = 3, max = 100, message = "Tên nhóm phải từ 3–100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @NotBlank(message = "Privacy là bắt buộc")
    @Pattern(regexp = "PUBLIC|PRIVATE",
            message = "Privacy chỉ được là PUBLIC hoặc PRIVATE"
    )
    private String privacy;

    @NotBlank(message = "Ảnh cover không được để trống")
    private String image;
}

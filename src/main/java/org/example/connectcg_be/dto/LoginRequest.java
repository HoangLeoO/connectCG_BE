package org.example.connectcg_be.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//Chứa: username, password
@Data
public class LoginRequest {
    @NotBlank
    private String username; // Có thể nhận cả email nếu muốn xử lý logic đó
    @NotBlank
    private String password;
}

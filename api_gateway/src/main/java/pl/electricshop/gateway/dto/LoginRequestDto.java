package pl.electricshop.gateway.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}

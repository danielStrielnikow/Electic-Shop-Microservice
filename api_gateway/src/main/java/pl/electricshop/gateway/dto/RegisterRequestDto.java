package pl.electricshop.gateway.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String email;
    private String password;
}

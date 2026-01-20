package pl.electricshop.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDto {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private UserInfoDto user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto {
        private String userId;
        private String email;
        private String role;
    }

    public static AuthResponseDto success(String message, String accessToken, String refreshToken, UserInfoDto user) {
        return AuthResponseDto.builder()
                .success(true)
                .message(message)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .build();
    }

    public static AuthResponseDto error(String message) {
        return AuthResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }
}

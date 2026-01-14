package pl.electricshop.user_service.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationResponse {

    private boolean success;
    private String message;

    public static OperationResponse success(String message) {
        return OperationResponse.builder()
                .success(true)
                .message(message)
                .build();
    }


    public static OperationResponse error(String message) {
        return OperationResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}

package pl.electicshop.user_service.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.electicshop.user_service.api.request.UpdateUserRoleRequest;
import pl.electicshop.user_service.api.response.OperationResponse;
import pl.electicshop.user_service.service.UserService;

/**
 * Admin Controller - Administrative operations
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * Update user role (ADMIN only)
     * Used to promote users to SELLER or ADMIN roles
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/role")
    public ResponseEntity<OperationResponse> updateUserRole(@Valid @RequestBody UpdateUserRoleRequest request) {
        userService.updateUserRole(request);
        return ResponseEntity.ok(
                OperationResponse.success("User role updated successfully to " + request.getRole())
        );
    }
}

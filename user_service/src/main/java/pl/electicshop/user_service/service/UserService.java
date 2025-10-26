package pl.electicshop.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.electicshop.user_service.api.request.UpdateUserRoleRequest;
import pl.electicshop.user_service.model.User;
import pl.electicshop.user_service.repository.UserRepository;

/**
 * User Service - Business logic for user management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Updates user role (ADMIN only operation)
     * Used to promote users to SELLER or ADMIN roles
     */
    @Transactional
    public void updateUserRole(UpdateUserRoleRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        user.setUserRole(request.getRole());
        userRepository.save(user);

        log.info("User role updated: {} -> {}", request.getEmail(), request.getRole());
    }
}

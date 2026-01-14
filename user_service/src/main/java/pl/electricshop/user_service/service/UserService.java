package pl.electricshop.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.electricshop.user_service.api.request.UpdateUserRoleRequest;
import pl.electricshop.user_service.api.response.UserResponse;
import pl.electricshop.user_service.mapper.UserMapper;
import pl.electricshop.user_service.model.User;
import pl.electricshop.user_service.repository.UserRepository;

import java.util.UUID;

/**
 * User Service - Business logic for user management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Get user by UUID
     */
    public UserResponse getUserById(UUID uuid) {
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + uuid));
        return userMapper.toResponse(user);
    }

    /**
     * Get user by email
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    /**
     * Get User entity by UUID (internal use)
     */
    public User getUserEntityById(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + uuid));
    }

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

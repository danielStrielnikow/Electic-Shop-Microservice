package pl.electicshop.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.electicshop.user_service.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}

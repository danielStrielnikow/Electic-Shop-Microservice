package pl.electicshop.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.electicshop.user_service.model.Address;

import java.util.UUID;

/**
 * Repository for Address entity
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
}

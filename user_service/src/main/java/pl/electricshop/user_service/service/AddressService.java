package pl.electricshop.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.electricshop.user_service.api.request.AddressRequest;
import pl.electricshop.user_service.api.request.UpdateAddressRequest;
import pl.electricshop.user_service.api.response.AddressResponse;
import pl.electricshop.user_service.exception.ResourceNotFoundException;
import pl.electricshop.user_service.mapper.AddressMapper;
import pl.electricshop.user_service.model.Address;
import pl.electricshop.user_service.model.User;
import pl.electricshop.user_service.repository.AddressRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Address Service - Business logic for address management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserService userService;

    /**
     * Get all addresses for a user
     */
    public List<AddressResponse> getUserAddresses(UUID userId) {
        User user = userService.getUserEntityById(userId);
        return user.getAddresses().stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get specific address by UUID
     * Validates that address belongs to the requesting user
     */
    public AddressResponse getAddressById(UUID addressId, UUID userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Verify address belongs to user
        if (!address.getUser().getUuid().equals(userId)) {
            throw new ResourceNotFoundException("Address not found");
        }

        return addressMapper.toResponse(address);
    }

    /**
     * Create new address for user
     */
    @Transactional
    public AddressResponse createAddress(AddressRequest request, UUID userId) {
        User user = userService.getUserEntityById(userId);

        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        Address savedAddress = addressRepository.save(address);
        log.info("Created address {} for user {}", savedAddress.getUuid(), userId);

        return addressMapper.toResponse(savedAddress);
    }

    /**
     * Update existing address
     * Validates that address belongs to the requesting user
     */
    @Transactional
    public AddressResponse updateAddress(UUID addressId, UpdateAddressRequest request, UUID userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Verify address belongs to user
        if (!address.getUser().getUuid().equals(userId)) {
            throw new ResourceNotFoundException("Address not found");
        }

        addressMapper.updateEntity(request, address);
        Address updatedAddress = addressRepository.save(address);

        log.info("Updated address {} for user {}", addressId, userId);

        return addressMapper.toResponse(updatedAddress);
    }

    /**
     * Delete address
     * Validates that address belongs to the requesting user
     */
    @Transactional
    public void deleteAddress(UUID addressId, UUID userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Verify address belongs to user
        if (!address.getUser().getUuid().equals(userId)) {
            throw new ResourceNotFoundException("Address not found");
        }

        addressRepository.delete(address);
        log.info("Deleted address {} for user {}", addressId, userId);
    }

    public AddressResponse getAddressByIdInternal(UUID addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        return addressMapper.toResponse(address);
    }
}

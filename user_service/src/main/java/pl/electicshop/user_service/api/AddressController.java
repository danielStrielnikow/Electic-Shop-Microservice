package pl.electicshop.user_service.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.electicshop.user_service.api.request.AddressRequest;
import pl.electicshop.user_service.api.request.UpdateAddressRequest;
import pl.electicshop.user_service.api.response.AddressResponse;
import pl.electicshop.user_service.api.response.OperationResponse;
import pl.electicshop.user_service.service.AddressService;

import java.util.List;
import java.util.UUID;

/**
 * Address Controller - Address management operations
 * All endpoints require authentication
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * Get all addresses for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get specific address by ID
     * Returns 404 if address doesn't exist or doesn't belong to user
     */
    @GetMapping("/user/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(
            @PathVariable UUID addressId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        AddressResponse address = addressService.getAddressById(addressId, userId);
        return ResponseEntity.ok(address);
    }

    /**
     * Create new address for authenticated user
     */
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        AddressResponse address = addressService.createAddress(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    /**
     * Update existing address
     * Returns 404 if address doesn't exist or doesn't belong to user
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        AddressResponse address = addressService.updateAddress(addressId, request, userId);
        return ResponseEntity.ok(address);
    }

    /**
     * Delete address
     * Returns 404 if address doesn't exist or doesn't belong to user
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<OperationResponse> deleteAddress(
            @PathVariable UUID addressId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        addressService.deleteAddress(addressId, userId);
        return ResponseEntity.ok(OperationResponse.success("Address deleted successfully"));
    }
}

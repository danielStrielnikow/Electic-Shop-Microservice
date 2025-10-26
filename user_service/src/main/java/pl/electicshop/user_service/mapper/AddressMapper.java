package pl.electicshop.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pl.electicshop.user_service.api.request.AddressRequest;
import pl.electicshop.user_service.api.request.UpdateAddressRequest;
import pl.electicshop.user_service.api.response.AddressResponse;
import pl.electicshop.user_service.model.Address;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    /**
     * Map Address entity to AddressResponse DTO
     */
    @Mapping(target = "userId", source = "user.uuid")
    AddressResponse toResponse(Address address);

    /**
     * Map AddressRequest to Address entity
     * User relationship will be set separately in service layer
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toEntity(AddressRequest request);

    /**
     * Update existing Address entity with values from UpdateAddressRequest
     * Only non-null values will be updated
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateAddressRequest request, @MappingTarget Address address);
}

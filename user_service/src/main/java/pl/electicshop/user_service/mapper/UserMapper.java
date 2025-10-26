package pl.electicshop.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pl.electicshop.user_service.api.request.RegisterRequest;
import pl.electicshop.user_service.api.request.UpdateUserRequest;
import pl.electicshop.user_service.api.request.UserRequest;
import pl.electicshop.user_service.api.response.UserResponse;
import pl.electicshop.user_service.model.User;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface UserMapper {

    /**
     * Map User entity to UserResponse DTO
     */
    UserResponse toResponse(User user);

    /**
     * Map RegisterRequest to User entity
     * Password will be encoded separately in service layer
     * UserRole is automatically set to USER for security reasons
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "userRole", constant = "USER")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);

    /**
     * Map UserRequest to User entity
     * Password will be encoded separately in service layer
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRequest request);

    /**
     * Update existing User entity with values from UpdateUserRequest
     * Only non-null values will be updated
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);

}

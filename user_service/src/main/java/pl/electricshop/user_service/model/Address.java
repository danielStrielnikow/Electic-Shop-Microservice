package pl.electricshop.user_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.electricshop.user_service.base.BaseEntity;
import pl.electricshop.user_service.model.enums.Country;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {

    @Column(name = "street")
    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 characters")
    private String street;

    @Column(name = "building_name")
    @NotBlank
    @Size(min = 5, message = "Building name must be at least 5 characters")
    private  String buildingName;

    @Column(name = "city")
    @NotBlank
    @Size(min = 4, message = "City name must be at least 4 characters")
    private String city;

    @Column(name = "state")
    @NotBlank
    @Size(min = 2, message = "State name must be at least 2 characters")
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(name = "country", nullable = false)
    private Country country;

    @Column(name = "pin_code")
    @NotBlank
    @Size(min = 5, message = "Pin-code must be at least 5 characters")
    private String pinCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

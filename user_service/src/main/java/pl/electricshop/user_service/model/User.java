package pl.electricshop.user_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.electricshop.user_service.base.BaseEntity;
import pl.electricshop.user_service.model.enums.Role;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role userRole;

    @Column(name= "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @OneToMany(mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    public User(String email, String password, Role userRole) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.emailVerified = false;
        this.addresses = new ArrayList<>();
    }
}

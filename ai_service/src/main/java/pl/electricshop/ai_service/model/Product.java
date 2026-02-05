package pl.electricshop.ai_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "product", schema = "products") // Wskazujemy konkretną tabelę
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    private UUID id;

    private String name;
    private String description;
}

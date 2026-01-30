package pl.electricshop.payment_service.model;

import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.electricshop.common.events.base.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {


    private String paymentMethod;

    private UUID orderId;

    private BigDecimal amount;

    private String currency;

    private String clientSecret;

    // Payment  gate response fields
    private String pgPaymentId;
    private String pgStatus;
    private String pgResponseMessage;
    private String pgName;

}

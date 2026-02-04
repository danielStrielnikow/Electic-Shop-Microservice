package pl.electricshop.payment_service.config;


import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        // To ustawia klucz globalnie dla całej aplikacji
        Stripe.apiKey = stripeApiKey;
    }
}

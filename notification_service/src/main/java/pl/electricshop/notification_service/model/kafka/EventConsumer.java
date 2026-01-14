package pl.electricshop.notification_service.model.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.electricshop.common.events.PasswordResetEvent;
import pl.electricshop.common.events.UserRegistrationEvent;
import pl.electricshop.notification_service.service.EmailService;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final EmailService emailService;

    @Bean
    public Consumer<UserRegistrationEvent> userRegistration() {
        return event -> {
            log.info("Otrzymano UserRegistrationEvent z Kafki: email={}", event.getEmail());
            emailService.sendEmailConfirmation(event.getEmail(), event.getVerificationToken());
        };
    }

    @Bean
    public Consumer<PasswordResetEvent> passwordReset() {
        return event -> {
            log.info("Otrzymano PasswordResetEvent z Kafki: email={}", event.getEmail());
            emailService.sendPasswordResetEmail(event.getEmail(), event.getVerificationToken());
        };
    }
}

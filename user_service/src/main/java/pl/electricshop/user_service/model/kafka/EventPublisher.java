package pl.electricshop.user_service.model.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import pl.electricshop.common.events.PasswordResetEvent;
import pl.electricshop.common.events.UserRegistrationEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final StreamBridge streamBridge;

    public void publishUserRegistration(UserRegistrationEvent event) {
        log.info("Publishing UserRegistrationEvent: email={}", event.getEmail());
        streamBridge.send("userRegistration-out-0", event);
    }

    public void publishPasswordReset(PasswordResetEvent event) {
        log.info("Publishing PasswordResetEvent: email={}", event.getEmail());
        streamBridge.send("passwordReset-out-0", event);
    }
}

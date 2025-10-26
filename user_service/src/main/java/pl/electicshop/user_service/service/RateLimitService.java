package pl.electicshop.user_service.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
@Slf4j
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private static final String EMAIL_VERIFICATION_PREFIX = "email_verification:";
    private static final int MAX_ATTEMPTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    public boolean isRateLimitedPassword(String email) {
        String key = PASSWORD_RESET_PREFIX + email;

        try {
            String currentCount = redisTemplate.opsForValue().get(key);

            if (currentCount == null) {
                // First request - set counter to 1 with TTl
                redisTemplate.opsForValue().set(key, "1", RATE_LIMIT_WINDOW);
                log.debug("First password reset request for email: {}", email);
                return false;
            }

            int count = Integer.parseInt(currentCount);
            if (count >= MAX_ATTEMPTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for email: {} (attempts: {})", email, count);
                return true;
            }

            // Increment counter (preserve existing TTL)
            redisTemplate.opsForValue().increment(key);
            log.debug("Password reset request count for email: {} is now {}", email, count + 1);
            return false;
        } catch (Exception e) {
            log.error("Password error during rate limit check for email: {}", email, e);
            // Fail open - allow request if Redis is unavailable
            return false;
        }
    }


        public boolean isEmailVerificationRateLimited(String email) {
            String key = EMAIL_VERIFICATION_PREFIX + email;

            try {
                String currentCount = redisTemplate.opsForValue().get(key);

                if (currentCount == null) {
                    redisTemplate.opsForValue().set(key, "1", RATE_LIMIT_WINDOW);
                    log.debug("First email verification request for email: {}", email);
                    return false;
                }

                int count = Integer.parseInt(currentCount);
                if (count >= MAX_ATTEMPTS_PER_MINUTE) {
                    log.warn("Email verification request count for email: {} is now {}", email, count + 1);
                    return true;
                }

                redisTemplate.opsForValue().increment(key);
                log.debug("Email verification request count for email: {} is now {}", email, count + 1);
                return false;

            } catch (Exception e) {
                log.error("Redis error during email verification rate limit check for email: {}", email, e);
                return false;
            }
        }
}



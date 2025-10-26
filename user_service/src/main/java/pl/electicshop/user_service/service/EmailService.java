package pl.electicshop.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.url.front}")
    private String baseUrl;

    @Value("${app.mail.password-reset.subject}")
    private String passwordResetSubject;

    @Value("${app.mail.password-reset.expiry-minutes}")
    private int expiryMinutes;

    @Value("${app.mail.email-verification.subject}")
    private String emailVerificationSubject;

    @Value("${app.mail.email-verification.expiry-minutes}")
    private int verificationExpiryMinutes;


    public void sendPasswordResetEmail(String email, String token) {
        String resetBaseUrl = baseUrl + "/reset-password";
        String link = resetBaseUrl + "/" + token;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);

        Context ctx = createEmailContext(link, expiresAt, email);
        String html = templateEngine.process("reset-password", ctx);
        sendHtml(email, passwordResetSubject, html);
    }

    public void sendEmailConfirmation(String email, String token) {
        String verifyBaseUrl = baseUrl + "/verify-email";
        String link = verifyBaseUrl + "/" + token;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(verificationExpiryMinutes);

        Context ctx = createEmailContext(link, expiresAt, email);
        String html = templateEngine.process("verify-email", ctx);
        sendHtml(email, emailVerificationSubject, html);

    }



    private Context createEmailContext(String link, LocalDateTime expiresAt, String email) {
        Context ctx = new Context();
        ctx.setVariable("ctaHref", link);
        ctx.setVariable("expiresAt", expiresAt);
        ctx.setVariable("email", email);
        return ctx;
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            h.setFrom(fromAddress);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send '{}' to {}", subject, to, e);
            throw new RuntimeException(e);
        }
    }
}

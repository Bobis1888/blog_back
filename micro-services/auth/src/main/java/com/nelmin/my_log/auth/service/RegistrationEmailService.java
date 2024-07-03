package com.nelmin.my_log.auth.service;

import com.nelmin.my_log.auth.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

// TODO refactor / rewrite
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationEmailService {

    @Value("${server.url:127.0.0.1}")
    private String serverUrl;

    //TODO config
    private final String emailQueue = "blog.email.send";

    private final JmsTemplate jmsTemplate;

    // TODO refactor
    public void sendResetEmail(String email, String uud) {
        String link = resolveUrl("?reset-password=" + uud);
        jmsTemplate.convertAndSend(
                emailQueue,
                new EmailMessage(
                        "Ссылка для сброса пароля",
                        buildResetPasswordHtml(link),
                        email)
        );
    }

    // TODO refactor
    public void sendConfirmEmail(String email, String uud) {
        String link = resolveUrl("api/auth/confirm?uuid=" + uud);

        jmsTemplate.convertAndSend(
                emailQueue,
                new EmailMessage(
                        "Подтвердите регистрацию на сайте",
                        buildConfirmEmailHtml(link),
                        email)
        );
    }

    // TODO template
    String buildConfirmEmailHtml(String url) {
        var builder = new StringBuilder();

        builder.append("<html>");
        builder.append("<body>");
        builder.append("<h4>Здравствуйте для подтверждения регистрации на сайте необходимо пройти по ссылке</h4>");
        builder.append("<div>");
        builder.append("<a href=\"");
        builder.append(url);
        builder.append("\">Ссылка для подтверждения</a>");
        builder.append("</div>");
        builder.append("</body>");
        builder.append("</html>");
        return builder.toString();
    }

    // TODO template
    String buildResetPasswordHtml(String url) {
        var builder = new StringBuilder();

        builder.append("<html>");
        builder.append("<body>");
        builder.append("<h4>Здравствуйте для сброса пароля на сайте необходимо пройти по ссылке</h4>");
        builder.append("<div>");
        builder.append("<a href=\"");
        builder.append(url);
        builder.append("\">Ссылка для сброса</a>");
        builder.append("</div>");
        builder.append("</body>");
        builder.append("</html>");
        return builder.toString();
    }

    private String resolveUrl(String paths) {
        return (serverUrl.endsWith("/") ? serverUrl : serverUrl  + "/") + paths;
    }
}

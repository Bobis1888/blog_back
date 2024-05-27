package com.nelmin.blog.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// TODO refactor / rewrite
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationEmailService {

    @Value("${server.address:127.0.0.1}")
    private String serverAddress;

    private final MailService mailService;

    // TODO refactor
    public void sendResetEmail(String email, String uud) {
        String link = serverAddress + "/auth/to-change-password?uuid=" + uud;

        mailService.sendMail(email,
                "Ссылка для сброса пароля",
                buildResetPasswordHtml(link));
    }

    // TODO refactor
    public void sendConfirmEmail(String email, String uud) {
        String link = serverAddress + "/auth/confirm?uuid=" + uud;

        mailService.sendMail(email,
                "Подтвердите регистрацию на сайте",
                buildConfirmEmailHtml(link));
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
}

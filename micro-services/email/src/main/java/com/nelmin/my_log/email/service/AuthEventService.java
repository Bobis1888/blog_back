package com.nelmin.my_log.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

// TODO refactor / rewrite
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEventService {

    @Value("${server.url:127.0.0.1}")
    private String serverUrl;

    private final MailService emailSender;

    // TODO refactor
    public void sendResetEmail(String email, String uuid) {
        String link = resolveUrl("?reset-password=" + uuid);
        emailSender.sendMail(
                email,
                "Ссылка для сброса пароля",
                buildResetPasswordHtml(link));
    }

    // TODO refactor
    public void sendConfirmEmail(String email, String uuid) {
        String link = resolveUrl("api/auth/confirm?uuid=" + uuid);
        emailSender.sendMail(
                email,
                "Подтвердите регистрацию на сайте",
                buildConfirmEmailHtml(link));
    }

    // TODO refactor
    public void sendOauthEmail(String email) {
        String link = resolveUrl("top");
        emailSender.sendMail(
                email,
                "Регистрация на сайте",
                buildOAuthHtml(link));
    }

    // TODO template
    public void sendBlockEmail(Map<String, String> payload) {
        emailSender.sendMail(
                payload.get("email"),
                "Вход заблокирован",
                buildBlockHtml(payload));
    }

    private String buildBlockHtml(Map<String, String> payload) {
        var builder = new StringBuilder();

        builder.append("<html>");
        builder.append("<body>");
        if (payload.get("reason").equals("attempts")) {
            builder.append("<h4>Превышено максимальное количество попыток входа</h4>");
        } else {
            builder.append("<h4>Вход заблокирован</h4>");
        }
        builder.append("<div>");

        builder.append("<span>Имя пользователя: ");
        builder.append(payload.get("email"));
        builder.append("</span>");
        builder.append("<br>");
        builder.append("<span>Адрес устройства: ");
        builder.append(payload.get("remoteAddress"));
        builder.append("</span>");
        builder.append("<br>");
        builder.append("<span>Заблокировано на: ");
        builder.append(payload.get("time"));
        builder.append(" мин.");
        builder.append("</span>");
        builder.append("<br>");
        builder.append("<span>Попробуйте сбросить пароль или зайти позже</span>");
        builder.append("<br>");
        builder.append("<a href=\"");
        builder.append(resolveUrl("auth/forgot-password?email=" + payload.get("email")));
        builder.append("\">Для сброса пароля пройдите по ссылке</a>");
        builder.append("</div>");
        builder.append("</body>");
        builder.append("</html>");
        return builder.toString();
    }

    // TODO template
    private String buildConfirmEmailHtml(String url) {
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
    private String buildResetPasswordHtml(String url) {
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

    // TODO template
    private String buildOAuthHtml(String url) {
        var builder = new StringBuilder();

        builder.append("<html>");
        builder.append("<body>");
        builder.append("<h4>Здравствуйте вы зарегистрировались на сайте Mylog</h4>");
        builder.append("<div>");
        builder.append("<a href=\"");
        builder.append(url);
        builder.append("\">Читать</a>");
        builder.append("</div>");
        builder.append("</body>");
        builder.append("</html>");
        return builder.toString();
    }

    private String resolveUrl(String paths) {
        return (serverUrl.endsWith("/") ? serverUrl : serverUrl + "/") + paths;
    }
}

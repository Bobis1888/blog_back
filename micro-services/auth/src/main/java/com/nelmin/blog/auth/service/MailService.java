package com.nelmin.blog.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${spring.mail.emulator:false}")
    private Boolean emulator;

    @Value("${spring.mail.sender:blog.auth@nelmin.com}")
    private String senderName;

    private final JavaMailSender javaMailSender;

    public void sendMail(String destination, String subject, String content) {
        log.info("Send email to {}", destination);

        if (emulator) {
            log.info("Emulator enabled, skip sending email");
            log.info("Subject : {}", subject);
            log.info("Content : {}", content);
            return;
        }

        var message = new SimpleMailMessage();
        message.setFrom(senderName);
        message.setTo(destination);
        message.setSubject(subject);
        message.setText(content);
        javaMailSender.send(message);
    }
}

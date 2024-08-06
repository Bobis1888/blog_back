package com.nelmin.my_log.common.service;

import com.nelmin.my_log.common.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {

    @Value("${spring.mail.mail_server.queue:blog.email.send}")
    private String emailQueue;

    private final JmsTemplate jmsTemplate;

    public void sendEmail(String email, String subject, String content) {
        log.info("Send email to {}", email);
        jmsTemplate.convertAndSend(emailQueue, new EmailMessage(subject, content, email));
    }
}

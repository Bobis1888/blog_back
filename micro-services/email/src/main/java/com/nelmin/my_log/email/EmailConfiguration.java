package com.nelmin.my_log.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfiguration {

    @Value("${spring.mail.host:}")
    private String emailHost;

    @Value("${spring.mail.port:}")
    private String emailPort;

    @Value("${spring.mail.username:}")
    private String emailUserName;

    @Value("${spring.mail.password:}")
    private String emailPassword;

    @Bean
    public JavaMailSender getJavaMailSender() {
        var javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(emailHost);
        javaMailSender.setPort(Integer.parseInt(emailPort));
        javaMailSender.setUsername(emailUserName);
        javaMailSender.setPassword(emailPassword);

        var properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "true");

        return javaMailSender;
    }
}

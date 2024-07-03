package com.nelmin.my_log.email;

import com.nelmin.my_log.email.dto.Message;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;

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

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public JmsTemplate jmsArtemisTemplate(ConnectionFactory jmsArtemisConnectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(jmsArtemisConnectionFactory);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setMessageConverter(messageConverter());
        return jmsTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        var converter = new MappingJackson2MessageConverter();
        converter.setTypeIdPropertyName("type");
        converter.setTypeIdMappings(Map.of("email", Message.class));
        return converter;
    }
}

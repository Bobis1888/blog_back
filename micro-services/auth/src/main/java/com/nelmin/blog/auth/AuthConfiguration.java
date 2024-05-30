package com.nelmin.blog.auth;

import com.nelmin.blog.auth.dto.EmailMessage;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

import java.util.Map;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.nelmin.blog", considerNestedRepositories = true)
public class AuthConfiguration {

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
        converter.setTypeIdMappings(Map.of("email", EmailMessage.class));
        return converter;
    }
}

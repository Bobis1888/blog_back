package com.nelmin.my_log.notification;

import com.nelmin.my_log.notification.dto.kafka.AuthEvent;
import com.nelmin.my_log.notification.dto.kafka.ContentEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class NotificationConfiguration {

    @Value("${spring.mail.host:}")
    private String emailHost;

    @Value("${spring.mail.port:}")
    private String emailPort;

    @Value("${spring.mail.username:}")
    private String emailUserName;

    @Value("${spring.mail.password:}")
    private String emailPassword;

    @Value("${spring.kafka.bootstrap.servers:127.0.0.1:9092}")
    private String bootstrapServers;

    @Value("${email.consumer.group.id:email-consumer}")
    private String groupId;

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
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TYPE_MAPPINGS,
                "auth-events:" + AuthEvent.class.getCanonicalName() + "," +
                "content-events:" + ContentEvent.class.getCanonicalName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

package com.nelmin.my_log.content;

import com.nelmin.my_log.content.dto.kafka.ContentEvent;
import com.nelmin.my_log.content.dto.kafka.UpdateImages;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(considerNestedRepositories = true)
public class ContentConfiguration {

    @Value("${content.events.topic:content-events}")
    private String eventsTopic;

    @Value("${spring.kafka.bootstrap.servers:127.0.0.1:9092}")
    private String bootstrapServers;

    @Value("${content.consumer.group.id:content-consumer}")
    private String groupId;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        var config = new HashMap<String, Object>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(config);
    }

    @Bean
    public NewTopic eventsTopic() {
        return TopicBuilder
                .name(eventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // TODO
    @Bean
    public NewTopic newTopics() {
        return TopicBuilder
                .name("image-events")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        var configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.TYPE_MAPPINGS,
                "image-events:" + UpdateImages.class.getCanonicalName() + "," +
                "content-events:" + ContentEvent.class.getCanonicalName());

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

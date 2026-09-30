package com.ecommerce.order.config;

import com.ecommerce.order.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    // ======================================================
    // OBJECT MAPPER
    // ======================================================

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules();
    }


    // ======================================================
    // CONSUMER FACTORY
    // ======================================================

    @Bean
    public ConsumerFactory<String, DomainEvent> consumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "kafka:9092"
        );

        config.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-service"
        );

        config.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        config.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JacksonJsonDeserializer.class
        );

        config.put(
                "spring.json.value.default.type",
                "com.ecommerce.order.event.DomainEvent"
        );

        config.put(
                "spring.json.trusted.packages",
                "com.ecommerce.order.event"
        );

        /*
         * Every microservice has its own DomainEvent class.
         * Therefore don't use the producer's Java type headers.
         */
        config.put(
                "spring.json.use.type.headers",
                false
        );

        /*
         * Useful while testing the Saga.
         */
        config.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return new DefaultKafkaConsumerFactory<>(config);
    }


    // ======================================================
    // LISTENER CONTAINER FACTORY
    // ======================================================

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, DomainEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, DomainEvent>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
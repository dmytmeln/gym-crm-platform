package com.gym.crm.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

import static org.springframework.jms.support.converter.MessageType.TEXT;

@Configuration
@EnableJms
public class JmsConfig {

    private static final String TYPE_ID_PROPERTY = "_type";

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(TEXT);
        converter.setTypeIdPropertyName(TYPE_ID_PROPERTY);

        return converter;
    }

}

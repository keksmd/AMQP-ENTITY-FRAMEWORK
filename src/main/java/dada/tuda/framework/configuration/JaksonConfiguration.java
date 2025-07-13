package dada.tuda.framework.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;

@AutoConfiguration(before = { JacksonAutoConfiguration.class })
public class JaksonConfiguration {
    @Bean
    @Primary
    @ConditionalOnMissingBean(value = ObjectMapper.class, annotation = Primary.class)
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        var module = new JavaTimeModule();
        mapper = mapper.registerModule(module);
        mapper = mapper.enable(INCLUDE_SOURCE_IN_LOCATION);
        mapper = mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper = mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper = mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        return mapper;
    }


}

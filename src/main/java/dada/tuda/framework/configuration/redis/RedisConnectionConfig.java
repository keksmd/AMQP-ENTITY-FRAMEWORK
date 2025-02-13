package dada.tuda.framework.configuration.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.configuration.jackson.JaksonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Import(JaksonConfiguration.class)
@Configuration
public class RedisConnectionConfig {
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public GenericJackson2JsonRedisSerializer serializer(@Autowired ObjectMapper objectMapper) {
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(@Value(value = "${spring.data.redis.host:localhost}") String host, @Value("${spring.data.redis.port:6379}") Integer port) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, GenericJackson2JsonRedisSerializer serializer) {
        var redis = new RedisTemplate<String, Object>();
        redis.setConnectionFactory(redisConnectionFactory);
        redis.setValueSerializer(serializer);
        redis.setHashValueSerializer(serializer);
        redis.setDefaultSerializer(serializer);
        redis.setEnableDefaultSerializer(true);
        redis.afterPropertiesSet();
        return redis;
    }
}

package dada.tuda.framework.conf;

import com.redis.testcontainers.RedisContainer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;


@Slf4j
@TestConfiguration
@EnableRedisRepositories
@ImportAutoConfiguration(RedisAutoConfiguration.class)
public class RedisContainerConfig {
    public static final RedisContainer redisContainer;

    static {
        redisContainer = new RedisContainer("redis:8.0.1");
        redisContainer.start();
        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString());
    }

    @PreDestroy
    public void stopContainer() {
        if (redisContainer.isRunning()) {
            redisContainer.stop();
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(Environment environment) {
        String host = environment.getProperty("spring.data.redis.host");
        int port = Integer.parseInt(environment.getProperty("spring.data.redis.port"));
        return new LettuceConnectionFactory(host, port);
    }


}

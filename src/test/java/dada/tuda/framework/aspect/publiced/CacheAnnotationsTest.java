package dada.tuda.framework.aspect.publiced;

import com.redis.testcontainers.RedisContainer;
import dada.tuda.framework.annotations.CacheWithDetails;
import dada.tuda.framework.annotations.DetailedCacheEvict;
import dada.tuda.framework.configuration.autoconfig.ConfigType;
import dada.tuda.framework.configuration.autoconfig.EnableCustomConfigs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertNotNull;


@Slf4j
@EnableCustomConfigs(ConfigType.CACHE_ANNOTATIONS)
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = {CacheAnnotationsTest.class, Service.class})
public class CacheAnnotationsTest {
    @Container
    public static RedisContainer redisContainer = new RedisContainer("redis:7.0").withExposedPorts(6379);
    @Autowired
    Service service;
    @Autowired
    CacheManager cacheManager;
    Dto dto1;
    Dto dto2;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getRedisHost);
        registry.add("spring.data.redis.port", redisContainer::getRedisPort);
    }

    @BeforeEach
    void setup() {
        dto1 = new Dto("1", "name1");
        dto2 = new Dto("2", "name2");
    }

    @Test
    void test() {
        service.save(List.of(dto1, dto2));
        service.getSaved(List.of(dto1.getId(), dto2.getId()));
        assertNotNull(cacheManager.getCache("list").get("1,2").get());
        assertNotNull(cacheManager.getCache("item").get("1").get());
        assertNotNull(cacheManager.getCache("item").get("2").get());
        service.removeById(dto1.getId());
        Assertions.assertNull(cacheManager.getCache("list").get("1,2"));
        Assertions.assertNull(cacheManager.getCache("item").get("1"));
        assertNotNull(cacheManager.getCache("item").get("2").get());
    }

    @Test
    void testDuplicate() {
        service.save(List.of(dto1, dto2));
        service.getSaved(List.of(dto1.getId(), dto2.getId()));
        assertNotNull(service.getSaved(List.of(dto1.getId(), dto2.getId())));
    }
}

@Component
class Service {
    final List<Dto> saved = new ArrayList<>();

    public void save(List<Dto> dtos) {
        saved.addAll(dtos);
    }

    @CacheWithDetails(
            listCache = "list",
            itemCache = "item",
            listKey = "#dtoIds",
            itemKeyField = "id"
    )
    public List<Dto> getSaved(List<String> dtoIds) {
        return saved.stream().filter(s -> dtoIds.contains(s.getId())).toList();
    }

    @DetailedCacheEvict(listCacheName = "list", itemCacheName = "item", listKey = "#id")
    public void removeById(String id) {
        saved.removeIf(d -> d.getId().equals(id));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Dto {
    private String id;
    private String name;
}





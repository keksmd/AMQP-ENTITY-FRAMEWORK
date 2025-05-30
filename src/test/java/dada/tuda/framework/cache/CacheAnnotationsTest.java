package dada.tuda.framework.cache;

import dada.tuda.framework.WholeConfig;
import dada.tuda.framework.conf.RedisContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
@ActiveProfiles("test")
@Testcontainers
@EnableCaching
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = { CacheAnnotationsTest.class, RedisContainerConfig.class, Service.class, WholeConfig.class })
public class CacheAnnotationsTest {
    @Autowired
    Service service;
    @Autowired
    CacheManager cacheManager;
    Dto dto1;
    Dto dto2;
    @Autowired
    private RedisTemplate redisTemplate;

    @AfterEach
    void cleanRedis() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().commands().flushDb();
        Objects.requireNonNull(cacheManager.getCache("item")).clear();
        Objects.requireNonNull(cacheManager.getCache("list")).clear();
    }

    @BeforeEach
    void setup() {
        service.clear();
        dto1 = new Dto("1", "name1");
        dto2 = new Dto("2", "name2");
    }

    @Test
    void testPartialHit() {
        service.save(List.of(dto1, dto2));
        service.getSaved(List.of(dto1.getId())); // кэшируется dto1
        service.getSaved(List.of(dto1.getId(), dto2.getId())); // dto2 ещё не в кэше

        assertNotNull(cacheManager.getCache("item").get(dto2.getId()));
        assertNotNull(cacheManager.getCache("list").get(dto1.getId() + "," + dto2.getId()));
    }

    @Test
    void testEmptyInput() {
        List<Dto> result = service.getSaved(List.of());
        assertTrue(result.isEmpty());
        assertNull(cacheManager.getCache("list").get(""));
    }

    @Test
    void checkReCacheWicthCacheble() {
        service.save(List.of(dto1, dto2));
        service.getSaved(List.of(dto1.getId(), dto2.getId()));
        Dto fromCachedble1 = service.getSaved(dto1.getId());
        Dto fromCachedble2 = service.getSaved(dto2.getId());
        assertEquals(fromCachedble1, dto1);
        assertEquals(fromCachedble2, dto2);
    }

    @Test
    void testPerformanceComparisonWithAndWithoutCache() {
        int count = 1000;
        List<Dto> dtos = IntStream.range(0, count).mapToObj(i -> new Dto(String.valueOf(i), "name" + i)).toList();

        service.save(dtos);
        List<String> ids = dtos.stream().map(Dto::getId).toList();

        // 1. Без кэша
        long startNoCache = System.currentTimeMillis();
        List<Dto> resultNoCache = service.getSavedNoCache(ids);
        long noCacheTime = System.currentTimeMillis() - startNoCache;

        // 2. С кэшем (первая загрузка + запись в кэш)
        long startFirstCache = System.currentTimeMillis();
        List<Dto> resultWithCache = service.getSaved(ids);
        long firstCacheTime = System.currentTimeMillis() - startFirstCache;

        // 3. С кэшем (повторное чтение — должно быть быстрее)
        long startSecondCache = System.currentTimeMillis();
        List<Dto> resultFromCache = service.getSaved(ids);
        long secondCacheTime = System.currentTimeMillis() - startSecondCache;

        // 4 без кэша (по-одному чтение — должно быть медленно)
        long startSecondCacheByOne = System.currentTimeMillis();
        ids.forEach(service::getSavedNoCache);
        long noCacheByOne = System.currentTimeMillis() - startSecondCacheByOne;

        // 5 кэшем (по-одному чтение — должно быть быстрее)
        long startSecondCacheByOne2 = System.currentTimeMillis();
        ids.forEach(service::getSaved);
        long cachebByOne = System.currentTimeMillis() - startSecondCacheByOne2;

        List<String> randomIds = Stream.generate(() -> String.valueOf(ThreadLocalRandom.current().nextInt(1, 999))).limit(count / 10).toList();
        // 6 чтение случайного списка с кэшем
        long randomsCached = System.currentTimeMillis();
        service.getSaved(randomIds);
        long randomsCachedT = System.currentTimeMillis() - randomsCached;

        // 7 чтение случайного списка без кэша
        long randomsNotCached = System.currentTimeMillis();
        service.getSavedNoCache(randomIds);
        long randomsNotCachedT = System.currentTimeMillis() - randomsNotCached;

        // Вывод
        System.out.println("No cache time:         " + noCacheTime + " ms");
        System.out.println("First cache fill time: " + firstCacheTime + " ms");
        System.out.println("Second cached read:    " + secondCacheTime + " ms");
        System.out.println("read by one :    " + noCacheByOne + " ms");
        System.out.println("Second cached read by one :    " + cachebByOne + " ms");
        System.out.println("read randoms cached list:    " + randomsCachedT + " ms");
        System.out.println("read randoms list :    " + randomsNotCachedT + " ms");


        // Утверждения
        assertEquals(resultNoCache.size(), count);
        assertEquals(resultWithCache.size(), count);
        assertEquals(resultFromCache.size(), count);

        // Важно: второе обращение должно быть быстрее первого и без кэша
        assertTrue(cachebByOne < noCacheByOne, "Кэш должен ускорить повторное чтение");
        assertTrue(randomsCachedT < randomsNotCachedT, "Кэш должен ускорить ассоциативное чтение");
        assertTrue(secondCacheTime < firstCacheTime, "Кэш должен заполняться с первого раза");
    }

    @Test
    void testPerformanceComparisonWithAndWithoutCachewithDeleting() {
        int count = 1000;
        List<Dto> dtos = new ArrayList<>(IntStream.range(0, count).mapToObj(i -> new Dto(String.valueOf(i), "name" + i)).toList());

        service.save(dtos);
        List<String> ids = dtos.stream().map(Dto::getId).toList();

        // 1. Без кэша
        long startNoCache = System.currentTimeMillis();
        List<Dto> resultNoCache = service.getSavedNoCache(ids);
        long noCacheTime = System.currentTimeMillis() - startNoCache;

        Dto toremove = dtos.get(0);
        dtos.remove(toremove);
        service.removeById(toremove.getId());

        // 2. С кэшем (первая загрузка + запись в кэш)
        long startFirstCache = System.currentTimeMillis();
        List<Dto> resultWithCache = service.getSaved(ids);
        long firstCacheTime = System.currentTimeMillis() - startFirstCache;

        toremove = dtos.get(0);
        dtos.remove(toremove);
        service.removeById(toremove.getId());

        // 3. С кэшем (повторное чтение — должно быть быстрее)
        long startSecondCache = System.currentTimeMillis();
        List<Dto> resultFromCache = service.getSaved(ids);
        long secondCacheTime = System.currentTimeMillis() - startSecondCache;

        toremove = dtos.get(0);
        dtos.remove(toremove);
        service.removeById(toremove.getId());

        // 4 без кэша (по-одному чтение — должно быть медленно)
        long startSecondCacheByOne = System.currentTimeMillis();
        ids.forEach(service::getSavedNoCache);
        long noCacheByOne = System.currentTimeMillis() - startSecondCacheByOne;

        toremove = dtos.get(0);
        dtos.remove(toremove);
        service.removeById(toremove.getId());

        // 5 кэшем (по-одному чтение — должно быть быстрее)
        long startSecondCacheByOne2 = System.currentTimeMillis();
        ids.forEach(service::getSaved);
        long cachebByOne = System.currentTimeMillis() - startSecondCacheByOne2;

        toremove = dtos.get(0);
        dtos.remove(toremove);
        service.removeById(toremove.getId());

        List<String> randomIds = new ArrayList<>(Stream.generate(() ->
                String.valueOf(ThreadLocalRandom.current().nextInt(1, 999))).limit(count / 10).toList());


        // 6 чтение случайного списка с кэшем
        long randomsCached = System.currentTimeMillis();
        var randomsCachedResult = service.getSaved(randomIds);
        long randomsCachedT = System.currentTimeMillis() - randomsCached;

        toremove = dtos.get(0);
        dtos.remove(toremove);
        service.removeById(toremove.getId());

        // 7 чтение случайного списка без кэша
        long randomsNotCached = System.currentTimeMillis();
        var randomsNotCachedResult = service.getSavedNoCache(randomIds);
        long randomsNotCachedT = System.currentTimeMillis() - randomsNotCached;

        // Вывод
        System.out.println("No cache get 1000 time:         " + noCacheTime + " ms");
        System.out.println("First cache fill + get 1000 time: " + firstCacheTime + " ms");
        System.out.println("Second cached read + get 1000:    " + secondCacheTime + " ms");
        System.out.println("read 1000 by one :    " + noCacheByOne + " ms");
        System.out.println("Cached read 1000 by one :    " + cachebByOne + " ms");
        System.out.println("read 100 randoms list :    " + randomsNotCachedT + " ms");
        System.out.println("read 100 randoms cached list:    " + randomsCachedT + " ms");

        // Утверждения
        assertEquals(resultNoCache.size(), count);
        assertEquals(resultWithCache.size(), count - 1);
        assertEquals(resultFromCache.size(), count - 2);

        // Важно: второе обращение должно быть быстрее первого и без кэша
        assertTrue(cachebByOne < noCacheByOne, "Кэш должен ускорить повторное чтение");

    }

    @Test
    void testPerformanceLowCoverage() {
        int count = 1000;
        List<Dto> dtos = new ArrayList<>(IntStream.range(0, count).mapToObj(i -> new Dto(String.valueOf(i), "name" + i)).toList());
        service.save(dtos);
        List<String> ids = dtos.stream().map(Dto::getId).toList();
        service.getSaved(ids);

        List<List<String>> listOfPairs =
                Stream.
                        generate(() ->
                                Stream
                                        .generate(() -> String.valueOf(ThreadLocalRandom.current().nextInt(1, 999)))
                                        .limit(2)
                                        .toList())
                        .limit(10)
                        .toList();

        List<Long> timesNoCache = new ArrayList<>();
        listOfPairs.forEach(list -> {
            long getTwoStart = System.currentTimeMillis();
            service.getSavedNoCache(list);
            long getTwoTime = System.currentTimeMillis() - getTwoStart;
            timesNoCache.add(getTwoTime);
        });
        Long maxNoCache = timesNoCache.stream().max(Long::compareTo).orElse(0L);
        long avgNoCache = timesNoCache.stream().mapToLong(Long::longValue).sum() / timesNoCache.size();

        List<Long> timesCachged = new ArrayList<>();
        listOfPairs.forEach(list -> {
            long getTwoStart = System.currentTimeMillis();
            service.getSaved(list);
            long getTwoTime = System.currentTimeMillis() - getTwoStart;
            timesCachged.add(getTwoTime);
        });
        Long maxCache = timesCachged.stream().max(Long::compareTo).orElse(0L);
        long avgCache = timesCachged.stream().mapToLong(Long::longValue).sum() / timesCachged.size();

        // Вывод
        System.out.println("No cache get 100 pairs time max / avg : " + maxNoCache + " / " + avgNoCache + " ms");
        System.out.println("With cache get 100 pairs time max / avg : " + maxCache + " / " + avgCache + " ms");


        assertTrue(maxCache < maxNoCache, "Кэш должен ускорить (max) повторное чтение");
        assertTrue(avgCache < avgNoCache, "Кэш должен ускорить (avg) повторное чтение");
    }

    @Test
    void testRemoveNonExistentId() {
        service.save(List.of(dto1));
        service.getSaved(List.of(dto1.getId()));
        service.removeById("999"); // не должен упасть
        assertNotNull(cacheManager.getCache("item").get(dto1.getId()));
    }

    @Test
    void testDuplicateKeys() {
        service.save(List.of(dto1));
        List<Dto> result = service.getSaved(List.of(dto1.getId(), dto1.getId()));
        assertEquals(1, result.size());
        assertNotNull(cacheManager.getCache("list").get(dto1.getId() + "," + dto1.getId()));
    }

    @Test
    void testMissingId() {
        service.save(List.of(dto1));
        List<Dto> result = service.getSaved(List.of(dto1.getId(), "999"));
        assertEquals(1, result.size(), "Ожидался один ответ");
        assertNotNull(cacheManager.getCache("item").get(dto1.getId()), " Ожидался dto1 в кэше");
        assertNull(cacheManager.getCache("item").get("999"), "Ожидалось отвутствие записи лишнего ключа");
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





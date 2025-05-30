package dada.tuda.framework.cache;

import dada.tuda.framework.annotations.CacheWithDetails;
import dada.tuda.framework.annotations.DetailedCacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Service {
    public static final long SLEEP_TIME = 2L;
    final List<Dto> saved = new ArrayList<>();

    public void save(List<Dto> dtos) {
        try {
            Thread.sleep(SLEEP_TIME * dtos.size());
        } catch (InterruptedException ignored) {
        }
        saved.addAll(dtos);
    }

    @CacheWithDetails(
            listCache = "list",
            itemCache = "item",
            listKey = "#dtoIds",
            itemKeyField = "id"
    )
    public List<Dto> getSaved(List<String> dtoIds) {
        return getSavedNoCache(dtoIds);
    }

    public List<Dto> getSavedNoCache(List<String> dtoIds) {
        return saved.stream().filter(s -> {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
            }
            return dtoIds.contains(s.getId());
        }).toList();
    }

    @Cacheable(key = "#dtoIds", cacheNames = "item", cacheManager = "redisCacheManager")
    public Dto getSaved(String dtoIds) {
        return getSavedNoCache(dtoIds);
    }

    public Dto getSavedNoCache(String dtoIds) {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
        }
        return saved.stream().filter(dto -> dtoIds.equals(dto.getId())).findFirst().orElse(null);
    }

    @DetailedCacheEvict(listCacheName = "list", itemCacheName = "item", listKey = "#id")
    public void removeById(String id) {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
        }
        saved.removeIf(d -> d.getId().equals(id));
    }

    public void clear() {
        this.saved.clear();
    }
}

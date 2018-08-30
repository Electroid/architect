package app.ashcon.architect.level;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class LevelCache {

    final LevelStore levelStore;
    final Cache<String, Optional<Level>> levelCache;

    @Inject LevelCache(LevelStore levelStore) {
        this.levelStore = levelStore;
        this.levelCache = CacheBuilder.newBuilder()
                                      .softValues()
                                      .initialCapacity(10)
                                      .expireAfterWrite(1, TimeUnit.MINUTES)
                                      .build(new CacheLoader<String, Optional<Level>>() {
                                          @Override
                                          public Optional<Level> load(String id) throws Exception {
                                              return levelStore.find(id);
                                          }
                                      });
    }

    public Optional<Level> find(String id) {
        Optional<Level> level = levelCache.getIfPresent(id);
        if(level == null) {
            level = levelStore.find(id);
            if(level.isPresent()) {
                levelCache.put(level.get().getId(), level);
            }
        }
        return level;
    }

    public void invalidate(String id) {
        levelCache.invalidate(id);
    }

}

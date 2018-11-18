package be.vbgn.gradle.pluginupdates.internal.cache;

import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.gradle.cache.CacheBuilder;
import org.gradle.cache.CacheBuilder.LockTarget;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.PersistentIndexedCacheParameters;
import org.gradle.internal.serialize.BaseSerializerFactory;

public class InvalidResolvesCache {
    private CacheBuilder cacheBuilder;

    private PersistentCache openedCache;

    private PersistentIndexedCache<Map<String, String>, Throwable> persistentIndexedCache;

    public InvalidResolvesCache(CacheRepository cacheRepository) {
        this.cacheBuilder = cacheRepository.cache("be.vbgn.gradle.pluginupdates")
                    .withCrossVersionCache(LockTarget.DefaultTarget)
                    .withProperties(Collections.singletonMap("cacheVersion", 1));
    }

    private synchronized void openCache() {
        if(openedCache == null) {
            openedCache = cacheBuilder.open();
        }
        if(persistentIndexedCache == null) {
            persistentIndexedCache = openedCache.createCache(PersistentIndexedCacheParameters.of("invalidResolves",
                            BaseSerializerFactory.NO_NULL_STRING_MAP_SERIALIZER,
                            BaseSerializerFactory.THROWABLE_SERIALIZER));
        }
    }

    private synchronized void closeCache() {
        if(openedCache != null) {
            openedCache.close();
        }
        openedCache = null;
        persistentIndexedCache = null;
    }


    private <T> T withCache(Function<PersistentIndexedCache<Map<String, String>, Throwable>, T> cacheHandler) {
        openCache();
        T returnValue = openedCache.useCache(() -> cacheHandler.apply(persistentIndexedCache));
        closeCache();
        return returnValue;
    }

    public void put(FailedDependency failedDependency) {
        withCache((cache) -> {
             cache.put(failedDependency.toDependencyNotation(), failedDependency.getProblem());
             return null;
        });
    }

    public Optional<FailedDependency> get(Dependency dependency) {
        Throwable error = withCache((cache) -> cache.get(dependency.toDependencyNotation()));
        if(error == null) {
            return Optional.empty();
        }
        return Optional.of(new DefaultFailedDependency(dependency.getGroup(), dependency.getName(), dependency.getVersion().toString(), error));
    }
}

package be.vbgn.gradle.pluginupdates.update.finder.internal;

import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheBuilder;
import org.gradle.cache.CacheBuilder.LockTarget;
import org.gradle.cache.CacheOpenException;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.FileLockManager.LockMode;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.PersistentIndexedCacheParameters;
import org.gradle.cache.internal.filelock.LockOptionsBuilder;
import org.gradle.internal.serialize.BaseSerializerFactory;

public class InvalidResolvesCache implements AutoCloseable {

    private static final Logger LOGGER = Logging.getLogger(InvalidResolvesCache.class);
    private CacheBuilder cacheBuilder;

    private PersistentCache openedCache;

    private PersistentIndexedCache<Map<String, String>, Throwable> persistentIndexedCache;

    public InvalidResolvesCache(CacheRepository cacheRepository) {
        this.cacheBuilder = cacheRepository.cache("be.vbgn.gradle.pluginupdates")
                .withCrossVersionCache(LockTarget.DefaultTarget)
                .withLockOptions(LockOptionsBuilder.mode(LockMode.Exclusive))
                .withProperties(Collections.singletonMap("cacheVersion", "1"));
    }

    private synchronized void openCache() {
        if(openedCache == null) {
            openedCache = cacheBuilder.open();
            LOGGER.debug("Opened cache {}", openedCache);
        }
        if(persistentIndexedCache == null) {
            persistentIndexedCache = openedCache.createCache(PersistentIndexedCacheParameters.of("invalidResolves",
                            BaseSerializerFactory.NO_NULL_STRING_MAP_SERIALIZER,
                            BaseSerializerFactory.THROWABLE_SERIALIZER));
            LOGGER.debug("Opened indexed cache {}", persistentIndexedCache);
        }
    }

    private synchronized void closeCache() {
        if(openedCache != null) {
            openedCache.close();
        }
        openedCache = null;
        persistentIndexedCache = null;
    }


    @Nullable
    private <T> T withCache(@Nonnull Function<PersistentIndexedCache<Map<String, String>, Throwable>, T> cacheHandler) {
        try {
            openCache();
            return openedCache.useCache(() -> cacheHandler.apply(persistentIndexedCache));
        } catch (CacheOpenException e) {
            LOGGER.warn("Invalid resolves cache could not be opened. Skipping use of cache.");
            LOGGER.debug("Full stacktrace for above warning", e);
            return null;
        }
    }

    public void put(FailedDependency failedDependency) {
        LOGGER.debug("Adding failed dependency {} to cache", failedDependency);
        withCache((cache) -> {
             cache.put(failedDependency.toDependencyNotation(), failedDependency.getProblem());
             return null;
        });
    }

    public Optional<FailedDependency> get(Dependency dependency) {
        Throwable error = withCache((cache) -> cache.get(dependency.toDependencyNotation()));
        if(error == null) {
            LOGGER.debug("Could not find failed dependency for {} in cache", dependency);
            return Optional.empty();
        }
        LOGGER.debug("Found failed dependency for {} in cache: {}", dependency, error);
        return Optional.of(new DefaultFailedDependency(dependency.getGroup(), dependency.getName(), dependency.getVersion().toString(), error));
    }

    @Override
    public void close() {
        this.closeCache();
    }
}

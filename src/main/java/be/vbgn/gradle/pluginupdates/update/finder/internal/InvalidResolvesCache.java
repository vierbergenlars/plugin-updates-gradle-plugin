package be.vbgn.gradle.pluginupdates.update.finder.internal;

import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

/**
 * Keeps a cache of dependencies for which resolving has failed, so they are not resolved every time
 */
public class InvalidResolvesCache {

    private static final Logger LOGGER = Logging.getLogger(InvalidResolvesCache.class);

    /**
     * Cache builder that is used to create an indexed key-value cache {@link #persistentIndexedCache} when needed
     */
    private CacheBuilder cacheBuilder;

    /**
     * File cache that contains the {@link #persistentIndexedCache}
     *
     * <b>Implementation note</b>
     *
     * Directly manipulated by {@link #openCache()} and {@link #closeCache()}.
     * Other usages should use the higher-level {@link #withCache(Function)} method
     */
    private PersistentCache openedCache;

    /**
     * Key-value cache that keeps track of dependencies that have failed to resolve
     */
    private PersistentIndexedCache<Dependency, Date> persistentIndexedCache;

    private long maxAge;

    public InvalidResolvesCache(CacheRepository cacheRepository) {
        this(cacheRepository, TimeUnit.DAYS.toNanos(1));
    }

    public InvalidResolvesCache(CacheRepository cacheRepository, long maxAge) {
        this.cacheBuilder = cacheRepository.cache("be.vbgn.gradle.pluginupdates")
                .withCrossVersionCache(LockTarget.DefaultTarget)
                .withLockOptions(LockOptionsBuilder.mode(LockMode.Exclusive))
                .withProperties(Collections.singletonMap("cacheVersion", "2"));
        this.maxAge = maxAge;
    }

    /**
     * Opens the invalid resolves cache
     *
     * @throws CacheOpenException When the cache can not be opened
     *
     * <b>Implementation note</b>
     *
     * This method is not thread safe and may only be called while holding a lock
     */
    private void openCache() throws CacheOpenException {
        if(openedCache == null) {
            openedCache = cacheBuilder.open();
            LOGGER.debug("Opened cache {}", openedCache);
        }
        if(persistentIndexedCache == null) {
            PersistentIndexedCacheParameters<Dependency, Date> cacheParameters = new PersistentIndexedCacheParameters<>(
                    "invalidResolves", Dependency.class, Date.class);
            persistentIndexedCache = openedCache.createCache(cacheParameters);
            LOGGER.debug("Opened indexed cache {} with parameters {}", persistentIndexedCache, cacheParameters);
        }
    }

    /**
     * Closes the invalid resolves cache
     *
     * <b>Implementation note</b>
     * This method is not thread safe and may only be called while holding a lock
     */
    private void closeCache() {
        if(openedCache != null) {
            openedCache.close();
        }
        openedCache = null;
        persistentIndexedCache = null;
    }


    /**
     * Runs an operation on the cache
     * <p>
     * Automatically opens the cache before the operation starts, and closes it after the operation has ended
     *
     * @param cacheHandler Function that will be run and passed a reference to the cache
     * @param <T>          Return type of the {@code cacheHandler} function
     * @return The value that {@code cacheHandler} returns, or null when the cache can not be opened
     */
    @Nullable
    private synchronized <T> T withCache(@Nonnull Function<PersistentIndexedCache<Dependency, Date>, T> cacheHandler) {
        try {
            openCache();
            return openedCache.useCache(() -> cacheHandler.apply(persistentIndexedCache));
        } catch (CacheOpenException e) {
            LOGGER.warn("Invalid resolves cache could not be opened. Skipping use of cache.");
            LOGGER.debug("Full stacktrace for above warning", e);
            return null;
        } finally {
            closeCache();
        }
    }

    /**
     * Places a new dependency in the failed resolves cache.
     *
     * @param dependency The dependency that has to be added to the list of failed resolves
     */
    public void put(Dependency dependency) {
        LOGGER.debug("Adding failed dependency {} to cache", dependency);
        withCache((cache) -> {
            cache.put(dependency, new Date());
            return null;
        });
    }

    /**
     * Tries to fetch a {@link FailedDependency} from cache for a {@link Dependency} that has failed.
     * <p>
     * Failures are only kept for a limited time, and are cleaned up after they have expired.
     *
     * @param dependency The dependency to look up a failure for
     * @return A failed dependency, or {@link Optional#empty()} when no failed dependency is found or its cache time has expired
     */
    public Optional<FailedDependency> get(Dependency dependency) {
        Date cacheValue = withCache((cache) -> cache.get(dependency));
        if (cacheValue == null) {
            LOGGER.debug("Could not find failed dependency for {} in cache", dependency);
            return Optional.empty();
        }
        if (cacheValue.getTime() <= new Date().getTime() - maxAge) {
            LOGGER.debug("Failed dependency for {} expired: {} if longer than {} µs ago", dependency,
                    cacheValue, maxAge);
            withCache(cache -> {
                cache.remove(dependency);
                return null;
            });
            return Optional.empty();
        }
        LOGGER.debug("Found failed dependency for {} in cache: {}", dependency, cacheValue);
        return Optional.of(new DefaultFailedDependency(dependency.getGroup(), dependency.getName(),
                dependency.getVersion().toString(), null));
    }

}

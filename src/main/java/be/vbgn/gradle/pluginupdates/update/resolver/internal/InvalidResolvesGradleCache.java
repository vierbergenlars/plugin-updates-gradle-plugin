package be.vbgn.gradle.pluginupdates.update.resolver.internal;

import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class InvalidResolvesGradleCache implements InvalidResolvesCache {

    private static final Logger LOGGER = Logging.getLogger(InvalidResolvesGradleCache.class);

    /**
     * Cache builder that is used to create an indexed key-value cache when needed
     */
    @Nonnull
    private CacheBuilder cacheBuilder;

    /**
     * Persistent indexed cache parameters for the key-value cache
     */
    private PersistentIndexedCacheParameters<Dependency, Date> persistentIndexedCacheParameters;

    private long maxAge;

    public InvalidResolvesGradleCache(@Nonnull CacheRepository cacheRepository) throws CacheNotAvailableException {
        this(cacheRepository, TimeUnit.DAYS.toMillis(1));
    }

    public InvalidResolvesGradleCache(@Nonnull CacheRepository cacheRepository, long maxAge)
            throws CacheNotAvailableException {
        this.cacheBuilder = cacheRepository.cache("be.vbgn.gradle.pluginupdates")
                .withCrossVersionCache(LockTarget.DefaultTarget)
                .withLockOptions(LockOptionsBuilder.mode(LockMode.Exclusive))
                .withProperties(Collections.singletonMap("cacheVersion", "2"));
        this.maxAge = maxAge;
        persistentIndexedCacheParameters = createIndexedCacheParameters();
    }

    private PersistentIndexedCacheParameters<Dependency, Date> createIndexedCacheParameters()
            throws CacheNotAvailableException {
        try {
            return createIndexedCacheParameters0();
        } catch (ReflectiveOperationException e) {
            throw new CacheNotAvailableException(e);
        }
    }

    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess", "JavaReflectionInvocation"})
    private PersistentIndexedCacheParameters<Dependency, Date> createIndexedCacheParameters0()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        try {
            // For gradle 5.1+
            Method constructionMethod = PersistentIndexedCacheParameters.class
                    .getMethod("of", String.class, Class.class, Class.class);
            return (PersistentIndexedCacheParameters) constructionMethod
                    .invoke("invalidResolves", Dependency.class, Date.class);
        } catch (NoSuchMethodException e) {
            // For gradle 4.3 - 5.0.+
            Constructor<PersistentIndexedCacheParameters> constructionMethod = PersistentIndexedCacheParameters.class
                    .getConstructor(String.class, Class.class, Class.class);
            return constructionMethod.newInstance("invalidResolves", Dependency.class, Date.class);
        }

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
        try (PersistentCache openedCache = cacheBuilder.open()) {
            LOGGER.debug("Opened cache {}", openedCache);
            PersistentIndexedCache<Dependency, Date> persistentIndexedCache = openedCache
                    .createCache(persistentIndexedCacheParameters);
            LOGGER.debug("Opened indexed cache {} with parameters {}", persistentIndexedCache,
                    persistentIndexedCacheParameters);
            return openedCache.useCache(() -> cacheHandler.apply(persistentIndexedCache));
        } catch (CacheOpenException e) {
            LOGGER.warn("Invalid resolves cache could not be opened. Skipping use of cache.");
            LOGGER.debug("Full stacktrace for above warning", e);
            return null;
        }
    }

    @Override
    public void put(Dependency dependency) {
        LOGGER.debug("Adding failed dependency {} to cache", dependency);
        withCache(cache -> {
            cache.put(dependency, new Date());
            return null;
        });
    }

    @Override
    public Optional<FailedDependency> get(Dependency dependency) {
        Date cacheValue = withCache(cache -> cache.get(dependency));
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

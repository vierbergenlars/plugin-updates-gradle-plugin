package be.vbgn.gradle.pluginupdates.update.resolver.internal;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.concurrent.TimeUnit;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.cache.CacheRepository;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class InvalidResolvesGradleCacheTest extends AbstractResolvesMemoryCacheTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private CacheRepository getCacheRepository() {
        Project project = ProjectBuilder.builder()
                .withGradleUserHomeDir(tempDir.getRoot())
                .build();
        return ((ProjectInternal)project).getServices().get(CacheRepository.class);
    }

    @Override
    protected InvalidResolvesCache createInvalidResolvesCache() throws CacheNotAvailableException {
        return new InvalidResolvesGradleCache(getCacheRepository());
    }

    @Test
    public void testCacheExpiry() throws InterruptedException, CacheNotAvailableException {
        long cacheTime = TimeUnit.SECONDS.toMillis(1);
        InvalidResolvesCache invalidResolvesCache = new InvalidResolvesGradleCache(getCacheRepository(), cacheTime);

        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test", "1.0.0");

        invalidResolvesCache.put(dependency);

        assertTrue(invalidResolvesCache.get(dependency).isPresent());

        // Wait for cache expire
        Thread.sleep(cacheTime*2);
        assertFalse(invalidResolvesCache.get(dependency).isPresent());
    }
}

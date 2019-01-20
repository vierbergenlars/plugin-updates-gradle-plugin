package be.vbgn.gradle.pluginupdates.update.resolver.internal;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import org.junit.Test;

public class InvalidResolvesMemoryCacheTest {
    @Test
    public void testCacheBehavior() {
        InvalidResolvesCache invalidResolvesCache = new InvalidResolvesMemoryCache();

        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test", "1.0.0");

        assertFalse(invalidResolvesCache.get(dependency).isPresent());

        invalidResolvesCache.put(dependency);

        assertTrue(invalidResolvesCache.get(dependency).isPresent());

        assertEquals(dependency, invalidResolvesCache.get(dependency).get());

        Dependency otherDependency = new DefaultDependency("be.vbgn.gradle", "test2", "2.0.0");

        assertFalse(invalidResolvesCache.get(otherDependency).isPresent());
    }

}

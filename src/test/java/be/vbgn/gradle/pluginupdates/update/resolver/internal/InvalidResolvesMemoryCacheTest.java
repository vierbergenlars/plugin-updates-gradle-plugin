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

        assertEquals(dependency.getGroup(), invalidResolvesCache.get(dependency).get().getGroup());
        assertEquals(dependency.getName(), invalidResolvesCache.get(dependency).get().getName());
        assertEquals(dependency.getVersion(), invalidResolvesCache.get(dependency).get().getVersion());
        assertEquals(dependency.getClassifier(), invalidResolvesCache.get(dependency).get().getClassifier());
        assertEquals(dependency.getType(), invalidResolvesCache.get(dependency).get().getType());

        Dependency otherDependency = new DefaultDependency("be.vbgn.gradle", "test2", "2.0.0");

        assertFalse(invalidResolvesCache.get(otherDependency).isPresent());
    }

}

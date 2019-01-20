package be.vbgn.gradle.pluginupdates.update.resolver;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesCache;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesMemoryCache;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.mockito.Mockito;

public class FailureCachingDependencyResolverTest {
    @Test
    public void passesThroughByDefault() {
        DependencyResolver backingResolver = Mockito.mock(DependencyResolver.class);
        InvalidResolvesCache invalidResolvesCache = new InvalidResolvesMemoryCache();

        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test", "1.0.+");

        Dependency resolvedDependency = dependency.withVersion("1.0.1");

        when(backingResolver.resolve(dependency)).then((invocation) -> Stream.of(resolvedDependency));

        DependencyResolver resolver = new FailureCachingDependencyResolver(backingResolver, invalidResolvesCache);

        // Resolve 2 times
        resolver.resolve(dependency).collect(Collectors.toList());
        List<Dependency> resolvedDependencies = resolver.resolve(dependency).collect(Collectors.toList());

        assertEquals(resolvedDependency, resolvedDependencies.get(0));

        verify(backingResolver, times(2)).resolve(dependency);
        verifyNoMoreInteractions(backingResolver);

        assertFalse(invalidResolvesCache.get(dependency).isPresent());
    }

    @Test
    public void failureWithEmptyCache() {
        DependencyResolver backingResolver = Mockito.mock(DependencyResolver.class);
        InvalidResolvesCache invalidResolvesCache = new InvalidResolvesMemoryCache();

        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test", "1.0.+");

        FailedDependency failedDependency = DefaultFailedDependency.fromDependency(dependency, new Throwable("marker"));

        when(backingResolver.resolve(dependency)).then((invocation) -> Stream.of(failedDependency));

        DependencyResolver resolver = new FailureCachingDependencyResolver(backingResolver, invalidResolvesCache);

        List<Dependency> resolvedDependencies = resolver.resolve(dependency).collect(Collectors.toList());

        assertTrue(resolvedDependencies.get(0) instanceof FailedDependency);

        verify(backingResolver).resolve(dependency);
        verifyNoMoreInteractions(backingResolver);
    }

    @Test
    public void failureWithFilledCache() {
        DependencyResolver backingResolver = Mockito.mock(DependencyResolver.class);
        InvalidResolvesCache invalidResolvesCache = new InvalidResolvesMemoryCache();

        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test", "1.0.+");

        FailedDependency failedDependency = DefaultFailedDependency.fromDependency(dependency, new Throwable("marker"));

        when(backingResolver.resolve(dependency)).then((invocation) -> Stream.of(failedDependency));

        DependencyResolver resolver = new FailureCachingDependencyResolver(backingResolver, invalidResolvesCache);

        resolver.resolve(dependency).collect(Collectors.toList()); // Resolve first time to get it in the cache
        List<Dependency> resolvedDependencies = resolver.resolve(dependency).collect(Collectors.toList());

        assertTrue(resolvedDependencies.get(0) instanceof FailedDependency);

        verify(backingResolver).resolve(dependency);
        verifyNoMoreInteractions(backingResolver);
    }

}

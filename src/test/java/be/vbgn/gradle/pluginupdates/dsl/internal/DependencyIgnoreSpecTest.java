package be.vbgn.gradle.pluginupdates.dsl.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.function.Predicate;
import org.junit.Test;

public class DependencyIgnoreSpecTest {
    @Test
    public void testGetFilterPredicate() {
        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test23", "1.2.3");

        DependencyIgnoreSpec ignoreSpec = new DependencyIgnoreSpec(dependency);

        Predicate<Dependency> filter = ignoreSpec.getFilterPredicate();

        assertFalse(filter.test(dependency));
        assertFalse(filter.test(dependency.withName("test23")));
        assertFalse(filter.test(dependency.withClassifier("sources")));
        assertFalse(filter.test(dependency.withType("jar")));

        assertTrue(filter.test(dependency.withVersion("4.5.6")));
        assertTrue(filter.test(dependency.withVersion("1.2.4")));
        assertTrue(filter.test(dependency.withGroup("be.vbgn.gradle.test")));
        assertTrue(filter.test(dependency.withName("test12")));
    }

    @Test
    public void testGetFilterPredicateWithRange() {
        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test23", "1.2.+");

        DependencyIgnoreSpec ignoreSpec = new DependencyIgnoreSpec(dependency);

        Predicate<Dependency> filter = ignoreSpec.getFilterPredicate();

        assertFalse(filter.test(dependency.withVersion("1.2.0")));
        assertFalse(filter.test(dependency.withVersion("1.2.4")));
        assertFalse(filter.test(dependency.withVersion("1.2.4.1")));

        assertTrue(filter.test(dependency.withVersion("4.5.6")));
        assertTrue(filter.test(dependency.withVersion("1.3.1")));
        assertTrue(filter.test(dependency.withVersion("1.0.0")));
    }

}

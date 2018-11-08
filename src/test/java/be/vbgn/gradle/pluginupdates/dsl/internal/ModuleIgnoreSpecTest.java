package be.vbgn.gradle.pluginupdates.dsl.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultModuleIdentifier;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.finder.FailureAllowedVersion;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.function.BiPredicate;
import org.junit.Test;

public class ModuleIgnoreSpecTest {

    @Test
    public void ignoreModule() {
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(new DefaultModuleIdentifier("be.vbgn.gradle", "test123"));

        BiPredicate<Dependency, FailureAllowedVersion> filter = ignoreSpec.getFilterPredicate();

        FailureAllowedVersion failureAllowedVersion = new FailureAllowedVersion(Version.parse("1.2.+"), true);

        assertFalse(filter.test(new DefaultDependency("be.vbgn.gradle", "test123", "1.2.0"), failureAllowedVersion));

        assertTrue(filter.test(new DefaultDependency("be.vbgn.gradle", "test", "1.2.0"), failureAllowedVersion));
        assertTrue(
                filter.test(new DefaultDependency("be.vbgn.gradle.test", "test123", "1.2.0"), failureAllowedVersion));
    }

    @Test
    public void ignoreMajor() {
        DefaultModuleIdentifier moduleIdentifier = new DefaultModuleIdentifier("be.vbgn.gradle", "test123");
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(moduleIdentifier);
        ignoreSpec.majorUpdates();

        BiPredicate<Dependency, FailureAllowedVersion> filter = ignoreSpec.getFilterPredicate();

        DefaultDependency dependency = new DefaultDependency("be.vbgn.gradle", "test123", "1.0.1.1");
        assertFalse(filter.test(dependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.1.1"), false)));

        Dependency dynamicDependency = dependency.withVersion("+");
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));

        dynamicDependency = dependency.withVersion("1.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));

        dynamicDependency = dependency.withVersion("1.1.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.+"), false)));

        dynamicDependency = dependency.withVersion("1.1.2.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.2.+"), false)));

        DefaultDependency dependency2 = dependency.withGroup("be.vbgn.gradle.test");
        assertTrue(filter.test(dependency2, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dependency2, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dependency2, new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(dependency2, new FailureAllowedVersion(Version.parse("1.2.3.+"), false)));
        assertTrue(filter.test(dependency2, new FailureAllowedVersion(Version.parse("1.2.3.4"), false)));

    }

    @Test
    public void ignoreMinor() {
        DefaultModuleIdentifier moduleIdentifier = new DefaultModuleIdentifier("be.vbgn.gradle", "test123");
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(moduleIdentifier);
        ignoreSpec.minorUpdates();

        BiPredicate<Dependency, FailureAllowedVersion> filter = ignoreSpec.getFilterPredicate();

        DefaultDependency dependency = new DefaultDependency("be.vbgn.gradle", "test123", "1.0.1.1");
        assertFalse(filter.test(dependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertFalse(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.1.1"), false)));

        Dependency dynamicDependency = dependency.withVersion("+");
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));

        dynamicDependency = dependency.withVersion("1.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));

        dynamicDependency = dependency.withVersion("1.1.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.+"), false)));

        dynamicDependency = dependency.withVersion("1.1.2.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.2.+"), false)));

        dependency = dependency.withGroup("be.vbgn.gradle.test");
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.2.3.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.2.3.4"), false)));
    }

    @Test
    public void ignoreMicro() {
        DefaultModuleIdentifier moduleIdentifier = new DefaultModuleIdentifier("be.vbgn.gradle", "test123");
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(moduleIdentifier);
        ignoreSpec.microUpdates();

        BiPredicate<Dependency, FailureAllowedVersion> filter = ignoreSpec.getFilterPredicate();

        DefaultDependency dependency = new DefaultDependency("be.vbgn.gradle", "test123", "1.0.1.1");

        assertFalse(filter.test(dependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertFalse(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertFalse(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.0.1.1"), false)));

        Dependency dynamicDependency = dependency.withVersion("+");
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));

        dynamicDependency = dependency.withVersion("1.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));

        dynamicDependency = dependency.withVersion("1.1.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.+"), false)));

        dynamicDependency = dependency.withVersion("1.1.2.+");
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertFalse(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(dynamicDependency, new FailureAllowedVersion(Version.parse("1.1.2.+"), false)));

        dependency = dependency.withGroup("be.vbgn.gradle.test");
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.2.3.+"), false)));
        assertTrue(filter.test(dependency, new FailureAllowedVersion(Version.parse("1.2.3.4"), false)));
    }

}

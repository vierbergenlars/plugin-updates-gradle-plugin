package be.vbgn.gradle.pluginupdates.dsl.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultModuleIdentifier;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultVersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.FailureAllowedVersion;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.junit.Test;

public class UpdatePolicyImplTest {

    @Test
    public void ignoreDependency() {
        UpdatePolicyImpl updatePolicy = new UpdatePolicyImpl();

        updatePolicy.ignore(new DefaultDependency("be.vbgn.gradle", "test", "1.3.4"));

        UpdateFinder baseUpdateFinder = new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                return Stream.of(dependency.withVersion("1.2.4"), dependency.withVersion("1.3.4"), dependency.withVersion("2.0.1"));
            }
        };

        UpdateFinder enhancedUpdateFinder = updatePolicy.buildUpdateFinder(baseUpdateFinder);

        List<Dependency> updates = enhancedUpdateFinder.findUpdates(new DefaultDependency("be.vbgn.gradle", "test", "1.2.3"))
            .collect(Collectors.toList());

        assertEquals(2, updates.size());
        assertEquals(new DefaultDependency("be.vbgn.gradle", "test", "1.2.4"), updates.get(0));
        assertEquals(new DefaultDependency("be.vbgn.gradle", "test", "2.0.1"), updates.get(1));

       updates = enhancedUpdateFinder.findUpdates(new DefaultDependency("be.vbgn.gradle", "test123", "1.2.3"))
                .collect(Collectors.toList());

        assertEquals(3, updates.size());
        assertEquals(new DefaultDependency("be.vbgn.gradle", "test123", "1.2.4"), updates.get(0));
        assertEquals(new DefaultDependency("be.vbgn.gradle", "test123", "1.3.4"), updates.get(1));
        assertEquals(new DefaultDependency("be.vbgn.gradle", "test123", "2.0.1"), updates.get(2));
    }

    @Test
    public void rename() {
        UpdatePolicyImpl updatePolicy = new UpdatePolicyImpl();

        updatePolicy.rename(new DefaultModuleIdentifier("be.vbgn.gradle", "test")).to(new DefaultModuleIdentifier("be.vbgn.gradle.test", "test123"));

        UpdateFinder baseUpdateFinder = new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                return Stream.of(dependency.withVersion("1.2.4"), dependency.withVersion("1.3.4"), dependency.withVersion("2.0.1"));
            }
        };

        UpdateFinder enhancedUpdateFinder = updatePolicy.buildUpdateFinder(baseUpdateFinder);

        List<Dependency> updates = enhancedUpdateFinder.findUpdates(new DefaultDependency("be.vbgn.gradle", "test", "1.2.3"))
                .collect(Collectors.toList());

        assertEquals(3, updates.size());
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test123", "1.2.4"), updates.get(0));
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test123", "1.3.4"), updates.get(1));
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test123", "2.0.1"), updates.get(2));

        updates = enhancedUpdateFinder.findUpdates(new DefaultDependency("be.vbgn.gradle.test", "test123", "1.2.3"))
                .collect(Collectors.toList());

        assertEquals(3, updates.size());
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test123", "1.2.4"), updates.get(0));
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test123", "1.3.4"), updates.get(1));
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test123", "2.0.1"), updates.get(2));

    }

    @Test
    public void ignoreModuleFully() {
        UpdatePolicyImpl updatePolicy = new UpdatePolicyImpl();

        updatePolicy.ignore(new DefaultModuleIdentifier("be.vbgn.gradle", "test"));

        VersionProvider baseVersionProvider = new DefaultVersionProvider();

        VersionProvider enhancedVersionProvider = updatePolicy.buildVersionProvider(baseVersionProvider);

        Set<Version> versions = enhancedVersionProvider.getUpdateVersions(new DefaultDependency("be.vbgn.gradle", "test", "1.2.3"))
                .map(FailureAllowedVersion::getVersion)
                .collect(Collectors.toSet());

        assertEquals(0, versions.size());

        versions = enhancedVersionProvider.getUpdateVersions(new DefaultDependency("be.vbgn.gradle", "test123", "1.2.3"))
                .map(FailureAllowedVersion::getVersion)
                .collect(Collectors.toSet());

        assertEquals(4, versions.size());
        assertTrue(versions.containsAll(Arrays.asList(
                Version.parse("+"),
                Version.parse("1.+"),
                Version.parse("1.2.+"),
                Version.parse("1.2.3.+")
        )));

    }

    @Test
    public void ignoreModulePartially() {
        UpdatePolicyImpl updatePolicy = new UpdatePolicyImpl();

        updatePolicy.ignore(new DefaultModuleIdentifier("be.vbgn.gradle", "testmajor")).majorUpdates();
        updatePolicy.ignore(new DefaultModuleIdentifier("be.vbgn.gradle", "testminor")).minorUpdates();
        updatePolicy.ignore(new DefaultModuleIdentifier("be.vbgn.gradle", "testmicro")).microUpdates();

        VersionProvider baseVersionProvider = new DefaultVersionProvider();

        VersionProvider enhancedVersionProvider = updatePolicy.buildVersionProvider(baseVersionProvider);

        Set<Version> versions = enhancedVersionProvider.getUpdateVersions(new DefaultDependency("be.vbgn.gradle", "testmajor", "1.2.3"))
                .map(FailureAllowedVersion::getVersion)
                .collect(Collectors.toSet());

        assertEquals(3, versions.size());
        assertTrue(versions.containsAll(Arrays.asList(
                Version.parse("1.+"),
                Version.parse("1.2.+"),
                Version.parse("1.2.3.+")
        )));

        versions = enhancedVersionProvider.getUpdateVersions(new DefaultDependency("be.vbgn.gradle", "testminor", "1.2.3"))
                .map(FailureAllowedVersion::getVersion)
                .collect(Collectors.toSet());

        assertEquals(2, versions.size());
        assertTrue(versions.containsAll(Arrays.asList(
                Version.parse("1.2.+"),
                Version.parse("1.2.3.+")
        )));

        versions = enhancedVersionProvider.getUpdateVersions(new DefaultDependency("be.vbgn.gradle", "testmicro", "1.2.3"))
                .map(FailureAllowedVersion::getVersion)
                .collect(Collectors.toSet());

        assertEquals(1, versions.size());
        assertTrue(versions.containsAll(Arrays.asList(
                Version.parse("1.2.3.+")
        )));

        versions = enhancedVersionProvider.getUpdateVersions(new DefaultDependency("be.vbgn.gradle", "test123", "1.2.3"))
                .map(FailureAllowedVersion::getVersion)
                .collect(Collectors.toSet());

        assertEquals(4, versions.size());
        assertTrue(versions.containsAll(Arrays.asList(
                Version.parse("+"),
                Version.parse("1.+"),
                Version.parse("1.2.+"),
                Version.parse("1.2.3.+")
        )));
    }


}

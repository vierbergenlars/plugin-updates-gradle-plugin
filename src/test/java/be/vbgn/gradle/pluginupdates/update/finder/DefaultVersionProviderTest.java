package be.vbgn.gradle.pluginupdates.update.finder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class DefaultVersionProviderTest {

    @Test
    public void generateVersions() {
        DefaultVersionProvider versionProvider = new DefaultVersionProvider();

        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test123", "1.2.3.4");
        List<FailureAllowedVersion> dependencies = versionProvider.getUpdateVersions(dependency)
                .collect(Collectors.toList());
        assertEquals(4, dependencies.size());
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.2.+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.2.3.+"), false)));

        dependency = dependency.withVersion("1.2.3");
        dependencies = versionProvider.getUpdateVersions(dependency).collect(Collectors.toList());
        assertEquals(4, dependencies.size());
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.2.+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.2.3.+"), true)));

        dependency = dependency.withVersion("1.2");
        dependencies = versionProvider.getUpdateVersions(dependency).collect(Collectors.toList());
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.2.+"), true)));

        dependency = dependency.withVersion("1");
        dependencies = versionProvider.getUpdateVersions(dependency).collect(Collectors.toList());
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.+"), true)));
    }

    @Test
    public void generateVersionsFromDynamic() {
        DefaultVersionProvider versionProvider = new DefaultVersionProvider();

        Dependency dependency = new DefaultDependency("be.vbgn.gradle", "test123", "1.2.3.+");
        List<FailureAllowedVersion> dependencies = versionProvider.getUpdateVersions(dependency)
                .collect(Collectors.toList());
        assertEquals(4, dependencies.size());
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.2.+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.2.3.+"), false)));

        dependency = dependency.withVersion("1.2.+");
        dependencies = versionProvider.getUpdateVersions(dependency).collect(Collectors.toList());
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.2.+"), false)));

        dependency = dependency.withVersion("1.+");
        dependencies = versionProvider.getUpdateVersions(dependency).collect(Collectors.toList());
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("1.+"), false)));

        dependency = dependency.withVersion("+");
        dependencies = versionProvider.getUpdateVersions(dependency).collect(Collectors.toList());
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains(new FailureAllowedVersion(Version.parse("+"), false)));

    }

}

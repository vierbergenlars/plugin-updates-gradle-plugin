package be.vbgn.gradle.pluginupdates.dsl.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import be.vbgn.gradle.pluginupdates.update.finder.FailureAllowedVersion;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.junit.Test;

public class ModuleIgnoreSpecTest {
    static class ModuleIdentifierImpl implements ModuleIdentifier {
        private String group;
        private String name;

        ModuleIdentifierImpl(String group, String name) {
            this.group = group;
            this.name = name;
        }

        @Nonnull
        @Override
        public String getGroup() {
            return group;
        }

        @Nonnull
        @Override
        public String getName() {
            return name;
        }
    }

    @Test
    public void ignoreModule() {
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(new ModuleIdentifierImpl("be.vbgn.gradle", "test123"));

        BiPredicate<ModuleIdentifier, FailureAllowedVersion> filter = ignoreSpec.getFilterPredicate();

        FailureAllowedVersion failureAllowedVersion = new FailureAllowedVersion(Version.parse("1.2.+"), true);

        assertFalse(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle", "test123"), failureAllowedVersion));

        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle", "test"), failureAllowedVersion));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), failureAllowedVersion));
    }

    @Test
    public void ignoreMajor() {
        ModuleIdentifierImpl moduleIdentifier = new ModuleIdentifierImpl("be.vbgn.gradle", "test123");
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(moduleIdentifier);
        ignoreSpec.majorUpdates();

        BiPredicate<ModuleIdentifier, FailureAllowedVersion> filter = ignoreSpec.getFilterPredicate();

        assertFalse(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.1.+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.1.1"), false)));

        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.2.3.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.2.3.4"), false)));

    }

    @Test
    public void ignoreMinor() {
        ModuleIdentifierImpl moduleIdentifier = new ModuleIdentifierImpl("be.vbgn.gradle", "test123");
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(moduleIdentifier);
        ignoreSpec.minorUpdates();

        BiPredicate<ModuleIdentifier, FailureAllowedVersion> filter = ignoreSpec.getFilterPredicate();

        assertFalse(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("+"), false)));
        assertFalse(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.1.+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.1.1"), false)));

        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.2.3.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.2.3.4"), false)));
    }

    @Test
    public void ignoreMicro() {
        ModuleIdentifierImpl moduleIdentifier = new ModuleIdentifierImpl("be.vbgn.gradle", "test123");
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(moduleIdentifier);
        ignoreSpec.microUpdates();

        BiPredicate<ModuleIdentifier, FailureAllowedVersion> filter = ignoreSpec.getFilterPredicate();

        assertFalse(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("+"), false)));
        assertFalse(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertFalse(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.1.+"), false)));
        assertTrue(filter.test(moduleIdentifier, new FailureAllowedVersion(Version.parse("1.0.1.1"), false)));

        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.1.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.2.3.+"), false)));
        assertTrue(filter.test(new ModuleIdentifierImpl("be.vbgn.gradle.test", "test123"), new FailureAllowedVersion(Version.parse("1.2.3.4"), false)));
    }
}

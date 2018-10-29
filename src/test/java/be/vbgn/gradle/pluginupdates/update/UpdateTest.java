package be.vbgn.gradle.pluginupdates.update;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.Test;

public class UpdateTest {

    @Test
    public void isOutdated() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.gradle.test", "test2", "0.1");
        Update update = new Update() {
            @Nonnull
            @Override
            public Dependency getOriginal() {
                return original;
            }

            @Nonnull
            @Override
            public List<Dependency> getUpdates() {
                return Collections.emptyList();
            }
        };

        assertFalse(update.isOutdated());

        update = new Update() {
            @Nonnull
            @Override
            public Dependency getOriginal() {
                return original;
            }

            @Nonnull
            @Override
            public List<Dependency> getUpdates() {
                return Collections.singletonList(original.withVersion("0.2"));
            }
        };

        assertTrue(update.isOutdated());
    }

    @Test
    public void isOutdatedFailedDependency() {
        Dependency original = new DefaultDependency("be.vbgn.gradle.test", "test", "0.1");

        Update update = new Update() {
            @Nonnull
            @Override
            public Dependency getOriginal() {
                return original;
            }

            @Nonnull
            @Override
            public List<Dependency> getUpdates() {
                return Collections.singletonList(
                        new DefaultFailedDependency("be.vbgn.gradle.test", "test", "0.1.+", new RuntimeException()));
            }
        };

        assertFalse(update.isOutdated());

        update = new Update() {
            @Nonnull
            @Override
            public Dependency getOriginal() {
                return original;
            }

            @Nonnull
            @Override
            public List<Dependency> getUpdates() {
                return Arrays.asList(
                        new DefaultFailedDependency("be.vbgn.gradle.test", "test", "0.1.+", new RuntimeException()),
                        original.withVersion("0.2")
                );
            }
        };

        assertTrue(update.isOutdated());
    }

}

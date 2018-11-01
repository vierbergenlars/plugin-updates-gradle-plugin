package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class DependencyIgnoreSpec extends AbstractIgnoreSpec {

    /**
     * Dependency that is the subject of this ignore rule
     */
    @Nonnull
    private Dependency subject;
    private static final Logger LOGGER = Logging.getLogger(DependencyIgnoreSpec.class);

    public DependencyIgnoreSpec(@Nonnull Dependency subject) {
        this.subject = subject;
    }

    @Nonnull
    public Predicate<Dependency> getFilterPredicate() {
        return dependency -> {
            if (subject.getGroup().equals(dependency.getGroup()) && subject.getName().equals(subject.getName())
                    && subject.getVersion().matches(
                    dependency.getVersion())) {
                LOGGER.debug("Ignore rule for {} removes update suggestion {}", subject, dependency);
                return false;
            }
            return true;
        };
    }

}

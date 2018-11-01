package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.finder.FailureAllowedVersion;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.io.Serializable;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class ModuleIgnoreSpec extends AbstractIgnoreSpec implements Serializable {

    /**
     * Module that is the subject of this ignore rule
     */
    @Nonnull
    private ModuleIdentifier subject;
    private static final Logger LOGGER = Logging.getLogger(ModuleIgnoreSpec.class);

    public ModuleIgnoreSpec(@Nonnull ModuleIdentifier subject) {
        this.subject = subject;
    }

    private boolean appliesToDependency(@Nonnull Dependency dependency) {
        return dependency.getGroup().equals(subject.getGroup()) && dependency.getName().equals(subject.getName());
    }

    private boolean appliesToVersionUpdate(@Nonnull Version version) {
        if (version.getMajor().hasWildcard() && ignoreMajorUpdates) {
            return true;
        }
        if (version.getMinor().hasWildcard() && ignoreMinorUpdates) {
            return true;
        }
        return version.getMicro().hasWildcard() && ignoreMicroUpdates;
    }

    @Nonnull
    public BiPredicate<Dependency, FailureAllowedVersion> getFilterPredicate() {
        return (dependency, failureAllowedVersion) -> {
            if (!appliesToDependency(dependency)) {
                return true;
            }
            boolean ignored = appliesToVersionUpdate(failureAllowedVersion.getVersion());
            if (ignored) {
                LOGGER.debug("Ignore rule for module {}:{} filtered out version {}", subject.getGroup(),
                        subject.getName(), failureAllowedVersion.getVersion());
            }
            return !ignored;
        };
    }
}

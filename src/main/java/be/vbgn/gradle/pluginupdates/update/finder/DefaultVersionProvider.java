package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.version.NumberWildcard;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class DefaultVersionProvider implements VersionProvider{
    private static final Logger LOGGER = Logging.getLogger(DefaultVersionProvider.class);

    @Nonnull
    @Override
    public Stream<FailureAllowedVersion> getUpdateVersions(@Nonnull Dependency dependency) {
        Version version = dependency.getVersion();
        LOGGER.debug("Generating update constraints for dependency {}", dependency);
        NumberWildcard wildcard = NumberWildcard.wildcard();
        Set<FailureAllowedVersion> versions = new HashSet<>();
        versions.add(new FailureAllowedVersion(version.withMajor(wildcard), version.getMajor().isEmpty()));
        if (!version.getMajor().isEmpty() && !version.getMajor().hasWildcard()) {
            versions.add(new FailureAllowedVersion(version.withMinor(wildcard), version.getMinor().isEmpty()));
        }
        if (!version.getMinor().isEmpty() && !version.getMinor().hasWildcard()) {
            versions.add(new FailureAllowedVersion(version.withMicro(wildcard), version.getMicro().isEmpty()));
        }
        if (!version.getMicro().isEmpty() && !version.getMicro().hasWildcard()) {
            versions.add(new FailureAllowedVersion(version.withPatch(wildcard), version.getPatch().isEmpty()));
        }
        return versions.stream()
                .distinct()
                .peek(version1 -> LOGGER.debug("Dependency {}: update constraint: {}, failure allowed: {}", dependency,
                        version1.getVersion(),
                        version1.isFailureAllowed()));
    }
}

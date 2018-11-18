package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.internal.cache.InvalidResolvesCache;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class FailedVersionsCachingVersionProvider implements VersionProvider {

    private static final Logger LOGGER = Logging.getLogger(FailedVersionsCachingVersionProvider.class);

    private VersionProvider versionProvider;
    private InvalidResolvesCache cache;

    public FailedVersionsCachingVersionProvider(
            VersionProvider versionProvider, InvalidResolvesCache cache) {
        this.versionProvider = versionProvider;
        this.cache = cache;
    }

    @Nonnull
    @Override
    public Stream<FailureAllowedVersion> getUpdateVersions(@Nonnull Dependency dependency) {
        return versionProvider.getUpdateVersions(dependency)
                .filter(failureAllowedVersion -> {
                    if(!failureAllowedVersion.isFailureAllowed()) {
                        return true;
                    }
                    Dependency proposedDependency = dependency.withVersion(failureAllowedVersion.getVersion());
                    if (cache.get(proposedDependency).isPresent()) {
                        LOGGER.debug(
                                "Excluding proposed dependency {} because cache contains previous resolve failure.",
                                dependency);
                        return false;
                    }
                    return true;
                });

    }
}

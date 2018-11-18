package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.internal.cache.InvalidResolvesCache;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class FailedVersionsCachingVersionProvider implements VersionProvider {

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
                    return !cache.get(dependency.withVersion(failureAllowedVersion.getVersion())).isPresent();
                });

    }
}

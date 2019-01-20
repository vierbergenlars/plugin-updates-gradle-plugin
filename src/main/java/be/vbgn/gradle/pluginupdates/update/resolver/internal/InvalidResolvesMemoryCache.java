package be.vbgn.gradle.pluginupdates.update.resolver.internal;

import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class InvalidResolvesMemoryCache implements InvalidResolvesCache {
    private Set<Dependency> failedDependencies = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void put(Dependency dependency) {
        failedDependencies.add(dependency);
    }

    @Override
    public Optional<FailedDependency> get(Dependency dependency) {
        if(failedDependencies.contains(dependency)) {
            return Optional.of(DefaultFailedDependency.fromDependency(dependency, null));
        }
        return Optional.empty();
    }
}

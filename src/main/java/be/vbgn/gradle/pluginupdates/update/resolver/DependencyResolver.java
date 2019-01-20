package be.vbgn.gradle.pluginupdates.update.resolver;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.stream.Stream;

public interface DependencyResolver {
    Stream<Dependency> resolve(Dependency dependency);
}

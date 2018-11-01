package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dsl.IgnoreSpec;
import be.vbgn.gradle.pluginupdates.dsl.RenameSpec;
import be.vbgn.gradle.pluginupdates.dsl.UpdatePolicy;
import be.vbgn.gradle.pluginupdates.update.finder.FailureAllowedVersion;
import be.vbgn.gradle.pluginupdates.update.finder.RenamedModuleFinder;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

public class UpdatePolicyImpl implements UpdatePolicy, UpdateBuilder, Serializable {

    private Set<ModuleIgnoreSpec> moduleIgnoreSpecs = new HashSet<>();
    private Set<DependencyIgnoreSpec> dependencyIgnoreSpecs = new HashSet<>();
    private List<ModuleRenameSpec> moduleRenameSpecs = new LinkedList<>();

    @Nonnull
    @Override
    public IgnoreSpec ignore(@Nonnull ModuleIdentifier module) {
        ModuleIgnoreSpec ignoreSpec = new ModuleIgnoreSpec(module);
        moduleIgnoreSpecs.add(ignoreSpec);
        return ignoreSpec;
    }

    @Nonnull
    @Override
    public IgnoreSpec ignore(@Nonnull Dependency dependency) {
        DependencyIgnoreSpec ignoreSpec = new DependencyIgnoreSpec(dependency);
        dependencyIgnoreSpecs.add(ignoreSpec);
        return ignoreSpec;
    }

    @Nonnull
    @Override
    public RenameSpec rename(@Nonnull ModuleIdentifier module) {
        ModuleRenameSpec renameSpec = new ModuleRenameSpec(module);
        moduleRenameSpecs.add(renameSpec);
        return renameSpec;
    }

    @Nonnull
    public VersionProvider buildVersionProvider(@Nonnull VersionProvider backingProvider) {
        Set<BiPredicate<Dependency, FailureAllowedVersion>> filterPredicates = moduleIgnoreSpecs.stream()
                .map(ModuleIgnoreSpec::getFilterPredicate)
                .collect(Collectors.toSet());

        BiPredicate<Dependency, FailureAllowedVersion> filterPredicate = (dependency, failureAllowedVersion) -> filterPredicates
                .stream().allMatch(predicate -> predicate.test(dependency, failureAllowedVersion));

        return new VersionProvider() {
            @Nonnull
            @Override
            public Stream<FailureAllowedVersion> getUpdateVersions(@Nonnull Dependency dependency) {
                return backingProvider.getUpdateVersions(dependency)
                        .filter(failureAllowedVersion -> filterPredicate.test(dependency, failureAllowedVersion));
            }
        };
    }

    @Nonnull
    public UpdateFinder buildUpdateFinder(@Nonnull UpdateFinder backingFinder) {
        UnaryOperator<Dependency> moduleRename = moduleRenameSpecs.stream()
                .map(ModuleRenameSpec::getTransformer)
                .reduce(UnaryOperator.identity(), (a, b) -> (dependency -> a.apply(b.apply(dependency))));
        UpdateFinder renamedModuleFinder = new RenamedModuleFinder(backingFinder, moduleRename);

        Set<Predicate<Dependency>> filterPredicates = dependencyIgnoreSpecs.stream()
                .map(DependencyIgnoreSpec::getFilterPredicate)
                .collect(Collectors.toSet());
        Predicate<Dependency> filterPredicate = dependency -> filterPredicates.stream()
                .allMatch(predicate -> predicate.test(dependency));
        return new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                return renamedModuleFinder.findUpdates(dependency)
                        .filter(filterPredicate);
            }
        };
    }
}


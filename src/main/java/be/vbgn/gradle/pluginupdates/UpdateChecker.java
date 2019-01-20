package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateBuilder;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerBuilderConfiguration;
import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.update.checker.DefaultUpdateChecker;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultUpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultVersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import be.vbgn.gradle.pluginupdates.update.resolver.DefaultDependencyResolver;
import be.vbgn.gradle.pluginupdates.update.resolver.DependencyResolver;
import be.vbgn.gradle.pluginupdates.update.resolver.FailureCachingDependencyResolver;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesCache;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesGradleCache;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesMemoryCache;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheRepository;

class UpdateChecker {

    private static final Logger LOGGER = Logging.getLogger(UpdateChecker.class);
    private Gradle gradle;
    private ConfigurationCollector configurationCollector;
    private InvalidResolvesCache invalidResolvesCache = null;


    public UpdateChecker(Gradle gradle, ConfigurationCollector configurationCollector) {
        this.gradle = gradle;
        this.configurationCollector = configurationCollector;
    }

    public List<Update> getUpdates(Project project, Configuration configuration) {
        UpdateCheckerBuilderConfiguration updateCheckerBuilderConfiguration = configurationCollector.forProject(project);
        UpdateBuilder updateBuilder = updateCheckerBuilderConfiguration.getUpdateBuilder();
        VersionProvider versionProvider = updateBuilder.buildVersionProvider(new DefaultVersionProvider());
        DependencyResolver defaultDependencyResolver = new DefaultDependencyResolver(project.getBuildscript());
        DependencyResolver cachedDependencyResolver = new FailureCachingDependencyResolver(defaultDependencyResolver,
                getInvalidResolvesCache());

        UpdateFinder updateFinder = updateBuilder
                .buildUpdateFinder(new DefaultUpdateFinder(cachedDependencyResolver, versionProvider));
        DefaultUpdateChecker updateChecker = new DefaultUpdateChecker(updateFinder);

        return updateChecker.getUpdates(configuration).collect(Collectors.toList());

    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private InvalidResolvesCache getInvalidResolvesCache() {
        if (invalidResolvesCache == null) {
            CacheRepository cacheRepository = ((GradleInternal) gradle).getServices()
                    .get(CacheRepository.class);
            try {
                if(classExists("org.gradle.cache.LockOptions")) {
                    invalidResolvesCache = new InvalidResolvesGradleCache(cacheRepository);
                } else {
                    invalidResolvesCache = new InvalidResolvesMemoryCache();
                }
            } catch (NoClassDefFoundError e) {
                LOGGER.warn(
                        "Some required gradle classes are missing. Invalid resolves cache is disabled, which will slow down plugin update checks.");
                LOGGER.debug("Full exception for above warning", e);
            }
        }
        return invalidResolvesCache;
    }
}

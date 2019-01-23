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
import be.vbgn.gradle.pluginupdates.update.resolver.internal.CacheNotAvailableException;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesCache;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesGradleCache;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesMemoryCache;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheRepository;

class UpdateChecker {

    private static final Logger LOGGER = Logging.getLogger(UpdateChecker.class);
    private static final String MISSING_CACHE_COMPONENT = "Some required gradle components are missing. Invalid resolves cache is disabled, which will slow down plugin update checks.";
    private CacheRepository cacheRepository;
    private ConfigurationCollector configurationCollector;
    private InvalidResolvesCache invalidResolvesCache = null;


    public UpdateChecker(CacheRepository cacheRepository, ConfigurationCollector configurationCollector) {
        this.cacheRepository = cacheRepository;
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
            try {
                if(classExists("org.gradle.cache.LockOptions")) {
                    invalidResolvesCache = new InvalidResolvesGradleCache(cacheRepository);
                } else {
                    invalidResolvesCache = new InvalidResolvesMemoryCache();
                }
            } catch (NoClassDefFoundError | CacheNotAvailableException e) {
                LOGGER.warn(MISSING_CACHE_COMPONENT);
                LOGGER.debug("Full exception for above warning", e);
                invalidResolvesCache = new InvalidResolvesMemoryCache();
            }
        }
        return invalidResolvesCache;
    }
}

package be.vbgn.gradle.pluginupdates.internal;

import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateBuilder;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerBuilderConfiguration;
import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.update.checker.DefaultUpdateChecker;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultUpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultVersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.internal.InvalidResolvesCache;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheRepository;

public class UpdateChecker {
    private static Logger LOGGER = Logging.getLogger(UpdateChecker.class);
    private Gradle gradle;
    private ConfigurationCollector configurationCollector;
    @Nullable
    private Optional<InvalidResolvesCache> invalidResolvesCache = null;


    public UpdateChecker(Gradle gradle, ConfigurationCollector configurationCollector) {
        this.gradle = gradle;
        this.configurationCollector = configurationCollector;
    }

    public List<Update> getUpdates(Project project, Configuration configuration) {
        UpdateCheckerBuilderConfiguration updateCheckerBuilderConfiguration = configurationCollector.forProject(project);
        UpdateBuilder updateBuilder = updateCheckerBuilderConfiguration.getUpdateBuilder();
        VersionProvider versionProvider = updateBuilder.buildVersionProvider(new DefaultVersionProvider());
        DefaultUpdateFinder defaultUpdateFinder = new DefaultUpdateFinder(project.getBuildscript(),
                versionProvider);
        UpdateFinder updateFinder = updateBuilder.buildUpdateFinder(defaultUpdateFinder);

        DefaultUpdateChecker updateChecker = new DefaultUpdateChecker(updateFinder);

        getInvalidResolvesCache()
                .ifPresent(defaultUpdateFinder::setInvalidResolvesCache);

        List<Update> updates = updateChecker.getUpdates(configuration).collect(Collectors.toList());

        return updates;

    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private Optional<InvalidResolvesCache> getInvalidResolvesCache() {
        if(invalidResolvesCache == null) {
            CacheRepository cacheRepository = ((GradleInternal) gradle).getServices()
                    .get(CacheRepository.class);
            invalidResolvesCache = Optional.empty();
            try {
                if(classExists("org.gradle.cache.LockOptions")) {
                    invalidResolvesCache = Optional.of(new InvalidResolvesCache(cacheRepository));
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

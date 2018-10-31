package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.update.checker.DefaultUpdateChecker;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultUpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultVersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class UpdateChecker {

    public static Logger LOGGER = Logging.getLogger(UpdateChecker.class);

    public static Stream<Update> checkBuildscriptUpdates(Project project) {
        return checkUpdates(project.getBuildscript(), project.getBuildscript().getConfigurations().getAt("classpath"));
    }

    public static Stream<Update> checkUpdates(ScriptHandler scriptHandler, Configuration configuration) {
        return checkUpdates(scriptHandler.getDependencies(), scriptHandler.getConfigurations(), configuration);
    }

    public static Stream<Update> checkUpdates(Project project) {
        ConfigurationContainer configurationContainer = project.getConfigurations();
        DependencyHandler dependencyHandler = project.getDependencies();
        return configurationContainer.stream()
                .filter(Configuration::isVisible)
                .flatMap(configuration -> checkUpdates(dependencyHandler, configurationContainer, configuration));
    }

    private static Stream<Update> checkUpdates(DependencyHandler dependencyHandler,
            ConfigurationContainer configurationContainer, Configuration configuration) {
        return createChecker(dependencyHandler, configurationContainer).getUpdates(configuration);
    }

    private static be.vbgn.gradle.pluginupdates.update.checker.UpdateChecker createChecker(
            DependencyHandler dependencyHandler,
            ConfigurationContainer configurationContainer) {
        VersionProvider versionProvider = new DefaultVersionProvider();
        UpdateFinder updateFinder = new DefaultUpdateFinder(dependencyHandler, configurationContainer, versionProvider);
        return new DefaultUpdateChecker(updateFinder);

    }
}


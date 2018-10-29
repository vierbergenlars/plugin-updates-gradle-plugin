package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.update.checker.DefaultUpdateChecker;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultUpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class UpdateChecker {

    public static Logger LOGGER = Logging.getLogger(UpdateChecker.class);

    public static Stream<Update> checkBuildscriptUpdates(Project project) {
        return checkUpdates(project, project.getBuildscript().getConfigurations().getAt("classpath"));
    }

    public static Stream<Update> checkUpdates(Project project) {
        return checkUpdates(project, project.getConfigurations());
    }

    public static Stream<Update> checkUpdates(Project project, ConfigurationContainer configurationContainer) {
        LOGGER.debug("Checking updates for {}, {}", project, configurationContainer);
        return configurationContainer.stream()
                .filter(Configuration::isVisible)
                .flatMap(configuration -> checkUpdates(project, configuration))
                .peek(update -> LOGGER.info("Found update {}", update));
    }

    public static Stream<Update> checkUpdates(Project project, Configuration configuration) {

        LOGGER.debug("Checking updates for {}, {}", project, configuration);

        UpdateFinder updateFinder = new DefaultUpdateFinder(project);
        be.vbgn.gradle.pluginupdates.update.checker.UpdateChecker updateChecker = new DefaultUpdateChecker(
                updateFinder);

        return updateChecker.getUpdates(configuration);
    }
}

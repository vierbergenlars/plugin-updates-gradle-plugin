package be.vbgn.gradle.pluginupdates.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import be.vbgn.gradle.pluginupdates.AbstractIntegrationTest;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class PluginUpdatesPolicySettingsTest extends AbstractIntegrationTest {

    @Test
    public void pluginUpdatesPolicySettings() throws IOException, URISyntaxException {
        // The plugin we apply in build.gradle does not support gradle < 4.0.0
        assumeTrue("Gradle version is at least 4.0.0",
                Version.parse(gradleVersion).getMajor().getNumberComponent() >= 4);
        BuildResult buildResult = buildProject(
                Paths.get(getClass().getResource("pluginUpdates-policy-settings").toURI()), "clean");

        Version gradleVersionInst = Version.parse(gradleVersion);
        // Gradle versions < 4.4 do not have the required api to read settings configuration
        if (gradleVersionInst.compareTo(Version.parse("4.4")) >= 0) {
            String[] outputLines = buildResult.getOutput().split("\n");
            List<String> pluginOutdatedLines = Arrays.stream(outputLines)
                    .filter(line -> line.startsWith("Plugin is outdated"))
                    .collect(Collectors.toList());

            assertEquals("There should only be one outdated plugin message", 1, pluginOutdatedLines.size());
            assertOutputContainsOneOf(buildResult,
                    "Plugin is outdated in root project 'pluginUpdates-policy': [eu.xenit.gradle:alfresco-sdk:0.1.3 -> org.gradle:gradle-hello-world-plugin:0.2]",
                    "Plugin is outdated in root project 'pluginUpdates-policy': [id 'eu.xenit.alfresco' version '0.1.3' -> id 'org.gradle.hello-world' version '0.2']"
            );
        } else {
            assertOutputContainsOneOf(buildResult,
                    "Plugin update configuration in settings.gradle can not be fetched and will be ignored.");
        }

    }


}

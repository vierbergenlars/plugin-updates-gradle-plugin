package be.vbgn.gradle.pluginupdates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import be.vbgn.gradle.pluginupdates.version.Version;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class IntegrationTest extends AbstractIntegrationTest {

    private Path integrationTests = Paths.get("src/integrationTest/resources/be/vbgn/gradle/pluginupdates/integration");

    @Test
    public void subprojects() throws IOException {
        BuildResult buildResult = buildProject(integrationTests.resolve("subprojects"), "clean");

        assertOutputContainsOneOf(buildResult,
                "Plugin is outdated in project ':subproject': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]",
                "Plugin is outdated in project ':subproject': id 'org.gradle.hello-world' version '[0.1 -> 0.2]'");

    }

    @Test
    public void projectPlugin() throws IOException {
        BuildResult buildResult = buildProject(integrationTests.resolve("project-plugin"), "clean");

        String[] outputLines = buildResult.getOutput().split("\n");

        List<String> pluginOutdatedLines = Arrays.stream(outputLines)
                .filter(line -> line.startsWith("Plugin is outdated"))
                .collect(Collectors.toList());

        assertEquals("There should only be one outdated plugin message", 1, pluginOutdatedLines.size());
    }

    @Test
    public void settingsPlugin() throws IOException {
        BuildResult buildResult = buildProject(integrationTests.resolve("settings-plugin"), "clean");

        String[] outputLines = buildResult.getOutput().split("\n");

        List<String> pluginOutdatedLines = Arrays.stream(outputLines)
                .filter(line -> line.startsWith("Plugin is outdated"))
                .collect(Collectors.toList());

        assertEquals("There should only be one outdated plugin message", 1, pluginOutdatedLines.size());
        assertOutputContainsOneOf(buildResult,
                "Plugin is outdated in root project 'settings-plugin': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]",
                "Plugin is outdated in root project 'settings-plugin': id 'org.gradle.hello-world' version '[0.1 -> 0.2]'");
    }

    @Test
    public void pluginUpdatesPolicy() throws IOException {
        assumeTrue("Gradle version is at least 4.0.0",
                Version.parse(gradleVersion).getMajor().getNumberComponent() >= 4);
        BuildResult buildResult = buildProject(integrationTests.resolve("pluginUpdates-policy"), "clean");

        String[] outputLines = buildResult.getOutput().split("\n");

        List<String> pluginOutdatedLines = Arrays.stream(outputLines)
                .filter(line -> line.startsWith("Plugin is outdated"))
                .collect(Collectors.toList());

        assertEquals("There should only be one outdated plugin message", 1, pluginOutdatedLines.size());
        assertOutputContainsOneOf(buildResult,
                "Plugin is outdated in root project 'pluginUpdates-policy': [eu.xenit.gradle:alfresco-sdk:0.1.3 -> org.gradle:gradle-hello-world-plugin:0.2]",
                "Plugin is outdated in root project 'pluginUpdates-policy': [id 'eu.xenit.alfresco' version '0.1.3' -> id 'org.gradle.hello-world' version '0.2']"
        );
    }

    @Test
    public void pluginUpdatesPolicyProject() throws IOException {
        // The plugin we apply in build.gradle does not support gradle < 4.0.0
        assumeTrue("Gradle version is at least 4.0.0",
                Version.parse(gradleVersion).getMajor().getNumberComponent() >= 4);
        BuildResult buildResult = buildProject(integrationTests.resolve("pluginUpdates-policy-project"), "clean");

        String[] outputLines = buildResult.getOutput().split("\n");

        List<String> pluginOutdatedLines = Arrays.stream(outputLines)
                .filter(line -> line.startsWith("Plugin is outdated"))
                .collect(Collectors.toList());

        assertEquals("There should only be one outdated plugin message", 1, pluginOutdatedLines.size());
        assertOutputContainsOneOf(buildResult,
                "Plugin is outdated in root project 'pluginUpdates-policy': [eu.xenit.gradle:alfresco-sdk:0.1.3 -> org.gradle:gradle-hello-world-plugin:0.2]",
                "Plugin is outdated in root project 'pluginUpdates-policy': [id 'eu.xenit.alfresco' version '0.1.3' -> id 'org.gradle.hello-world' version '0.2']"
        );

    }

    @Test
    public void pluginUpdatesPolicySettings() throws IOException {
        // The plugin we apply in build.gradle does not support gradle < 4.0.0
        assumeTrue("Gradle version is at least 4.0.0",
                Version.parse(gradleVersion).getMajor().getNumberComponent() >= 4);
        BuildResult buildResult = buildProject(integrationTests.resolve("pluginUpdates-policy-settings"), "clean");

        Version gradleVersionInst = Version.parse(gradleVersion);
        // Gradle versions < 4.3 do not have the required api to read settings configuration
        if (Version.parse("4.3").compareTo(gradleVersionInst) < 0) {
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

    @Test
    public void warnOnceAboutUnsupportedSettings() throws IOException {
        // Gradle versions < 4.3 do not have the required api to read settings configuration
        assumeTrue("Gradle version is at most 4.3", Version.parse(gradleVersion).compareTo(Version.parse("4.3")) < 0);

        BuildResult buildResult = buildProject(integrationTests.resolve("subprojects"), "clean");

        String[] lines = buildResult.getOutput().split("\n");

        boolean foundWarning = false;
        for (String line : lines) {
            if (line.contains(
                    "Plugin update configuration in settings.gradle can not be fetched and will be ignored.")) {
                assertFalse("The warning message can only be present once.", foundWarning);
                foundWarning = true;
            }

        }
        assertTrue("The warning message must be present once.", foundWarning);
    }

}

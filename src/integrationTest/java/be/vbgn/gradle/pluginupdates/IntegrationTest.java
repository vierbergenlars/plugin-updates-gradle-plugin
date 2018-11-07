package be.vbgn.gradle.pluginupdates;

import static org.junit.Assert.assertEquals;
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
                "Plugin is outdated in project ':subproject': org.gradle.hello-world:org.gradle.hello-world.gradle.plugin:[0.1 -> 0.2]");

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
                "Plugin is outdated in root project 'settings-plugin': org.gradle.hello-world:org.gradle.hello-world.gradle.plugin:[0.1 -> 0.2]");
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
                "Plugin is outdated in root project 'pluginUpdates-policy': [eu.xenit.alfresco:eu.xenit.alfresco.gradle.plugin:0.1.3 -> org.gradle.hello-world:org.gradle.hello-world.gradle.plugin:0.2]"
        );
    }

    @Test
    public void pluginUpdatesPolicyProject() throws IOException {
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
                "Plugin is outdated in root project 'pluginUpdates-policy': [eu.xenit.alfresco:eu.xenit.alfresco.gradle.plugin:0.1.3 -> org.gradle.hello-world:org.gradle.hello-world.gradle.plugin:0.2]"
        );

    }

}

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

public class PluginUpdatesPolicyTest extends AbstractIntegrationTest {

    @Test
    public void pluginUpdatesPolicy() throws IOException, URISyntaxException {
        assumeTrue("Gradle version is at least 4.0.0",
                Version.parse(gradleVersion).getMajor().getNumberComponent() >= 4);
        BuildResult buildResult = buildProject(Paths.get(getClass().getResource("pluginUpdates-policy").toURI()),
                "clean");

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

}

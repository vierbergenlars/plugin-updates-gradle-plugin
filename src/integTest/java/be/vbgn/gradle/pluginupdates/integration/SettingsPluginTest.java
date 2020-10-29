package be.vbgn.gradle.pluginupdates.integration;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.AbstractIntegrationTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class SettingsPluginTest extends AbstractIntegrationTest {

    @Test
    public void settingsPlugin() throws IOException, URISyntaxException {
        BuildResult buildResult = buildProject(Paths.get(getClass().getResource("settings-plugin").toURI()), "clean");

        String[] outputLines = buildResult.getOutput().split("\n");

        List<String> pluginOutdatedLines = Arrays.stream(outputLines)
                .filter(line -> line.startsWith("Plugin is outdated"))
                .collect(Collectors.toList());

        assertEquals("There should only be one outdated plugin message", 1, pluginOutdatedLines.size());
        assertOutputContainsOneOf(buildResult,
                "Plugin is outdated in root project 'settings-plugin': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]",
                "Plugin is outdated in root project 'settings-plugin': id 'org.gradle.hello-world' version '[0.1 -> 0.2]'");
    }

}

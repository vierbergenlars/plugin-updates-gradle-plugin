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

public class ProjectPluginTest extends AbstractIntegrationTest {

    @Test
    public void projectPlugin() throws IOException, URISyntaxException {
        BuildResult buildResult = buildProject(Paths.get(getClass().getResource("project-plugin").toURI()), "clean");

        String[] outputLines = buildResult.getOutput().split("\n");

        List<String> pluginOutdatedLines = Arrays.stream(outputLines)
                .filter(line -> line.startsWith("Plugin is outdated"))
                .collect(Collectors.toList());

        assertEquals("There should only be one outdated plugin message", 1, pluginOutdatedLines.size());
    }

}

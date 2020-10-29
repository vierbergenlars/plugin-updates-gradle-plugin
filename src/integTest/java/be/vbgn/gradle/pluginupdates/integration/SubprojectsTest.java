package be.vbgn.gradle.pluginupdates.integration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import be.vbgn.gradle.pluginupdates.AbstractIntegrationTest;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class SubprojectsTest extends AbstractIntegrationTest {

    @Test
    public void subprojects() throws IOException, URISyntaxException {
        BuildResult buildResult = buildProject(Paths.get(getClass().getResource("subprojects").toURI()), "clean");

        assertOutputContainsOneOf(buildResult,
                "Plugin is outdated in project ':subproject': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]",
                "Plugin is outdated in project ':subproject': id 'org.gradle.hello-world' version '[0.1 -> 0.2]'");
    }

    /**
     * https://github.com/vierbergenlars/plugin-updates-gradle-plugin/issues/3
     */
    @Test
    public void warnOnceAboutUnsupportedSettings() throws IOException, URISyntaxException {
        // Gradle versions < 4.4 do not have the required api to read settings configuration
        assumeTrue("Gradle version is at most 4.3", Version.parse(gradleVersion).compareTo(Version.parse("4.4")) < 0);

        BuildResult buildResult = buildProject(Paths.get(getClass().getResource("subprojects").toURI()), "clean");

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

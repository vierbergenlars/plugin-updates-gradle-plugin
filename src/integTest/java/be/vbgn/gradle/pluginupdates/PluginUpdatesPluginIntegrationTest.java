package be.vbgn.gradle.pluginupdates;

import static be.vbgn.gradle.pluginupdates.TestUtil.writeFile;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PluginUpdatesPluginIntegrationTest extends AbstractIntegrationTest {

    private File buildFile;
    private File settingsFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
        settingsFile = testProjectDir.newFile("settings.gradle");
    }

    @Test
    public void applyProjectPlugins() throws Exception {
        writeFile(buildFile, "plugins {\n"
                + "id 'base'\n"
                + "id 'org.gradle.hello-world' version '0.1'\n"
                + "id 'be.vbgn.plugin-updates'\n"
                + "}\n");
        writeFile(settingsFile, "rootProject.name = 'test-project'");
        BuildResult buildResult = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.getRoot())
                .withGradleVersion(gradleVersion)
                .forwardOutput()
                .withArguments("clean")
                .build();

        assertOutputContainsOneOf(buildResult,
                "Plugin is outdated in root project 'test-project': id 'org.gradle.hello-world' version '[0.1 -> 0.2]'",
                "Plugin is outdated in root project 'test-project': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]"
        );
    }

    @Test
    public void applyProjectWithClassifiers() throws Exception {
        writeFile(buildFile, "buildscript {\n"
                + "repositories {\n"
                + "maven {\n"
                + "url 'https://plugins.gradle.org/m2'\n"
                + "}\n"
                + "}\n"
                + "dependencies {\n"
                + "classpath 'org.gradle:gradle-hello-world-plugin:0.1:sources'\n"
                + "classpath 'org.gradle:gradle-hello-world-plugin:0.1@pom'\n"
                + "classpath 'org.gradle:gradle-hello-world-plugin:0.1'\n"
                + "}\n"
                + "}\n"
                + "plugins {\n"
                + "id 'base'\n"
                + "id 'be.vbgn.plugin-updates'\n"
                + "}\n"
        );
        writeFile(settingsFile, "rootProject.name = 'test-project'");
        BuildResult buildResult = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.getRoot())
                .withGradleVersion(gradleVersion)
                .withDebug(true)
                .forwardOutput()
                .withArguments("clean")
                .build();
        assertTrue(buildResult.getOutput()
                .contains(
                        "Plugin is outdated in root project 'test-project': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]"));
        assertTrue(buildResult.getOutput()
                .contains(
                        "Plugin is outdated in root project 'test-project': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]@pom"));

        assertTrue(buildResult.getOutput()
                .contains(
                        "Plugin is outdated in root project 'test-project': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]:sources"));
    }


}

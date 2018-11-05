package be.vbgn.gradle.pluginupdates;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PluginUpdatesPluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;
    private File settingsFile;

    @Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"4.10"}, {"4.0"}, {"3.2.1"}
        });
    }

    private String gradleVersion;

    public PluginUpdatesPluginTest(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }

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

        String[] expectedMessages = {
                "Plugin is outdated in root project 'test-project': org.gradle.hello-world:org.gradle.hello-world.gradle.plugin:[0.1 -> 0.2]",
                "Plugin is outdated in root project 'test-project': org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]"
        };

        boolean matched = false;
        for (String expectedMessage : expectedMessages) {
            matched |= buildResult.getOutput().contains(expectedMessage);
        }
        assertTrue(matched);

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

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

}

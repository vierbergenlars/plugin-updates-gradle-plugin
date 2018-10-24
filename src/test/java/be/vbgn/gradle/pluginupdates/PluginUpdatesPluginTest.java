package be.vbgn.gradle.pluginupdates;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PluginUpdatesPluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;
    private File settingsFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
        settingsFile = testProjectDir.newFile("settings.gradle");
    }

    @Test
    public void applyProject() throws Exception {
        writeFile(buildFile, "plugins {\n"
                + "id 'java'\n"
                + "id 'org.gradle.hello-world' version '0.1'\n"
                + "id 'be.vbgn.plugin-updates'\n"
                + "}\n");
        writeFile(settingsFile, "rootProject.name = 'test-project'");
        BuildResult buildResult = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.getRoot())
                .forwardOutput()
                .withDebug(true)
                .withArguments("clean")
                .build();

        assertTrue(buildResult.getOutput()
                .contains(
                        "Plugin is outdated in project test-project: org.gradle.hello-world:org.gradle.hello-world.gradle.plugin:[0.1 -> 0.2]"));
    }

    @Test
    public void applyProjectWithClassifiers() throws Exception {
        writeFile(buildFile, "buildscript {\n"
                + "dependencies {\n"
                + "classpath 'org.gradle:gradle-hello-world-plugin:0.1:sources'\n"
                + "classpath 'org.gradle:gradle-hello-world-plugin:0.1@pom'\n"
                + "classpath 'org.gradle:gradle-hello-world-plugin:0.1'\n"
                + "}\n"
                + "}\n"
                + "plugins {\n"
                + "id 'java'\n"
                + "id 'be.vbgn.plugin-updates'\n"
                + "}\n"
        );
        writeFile(settingsFile, "rootProject.name = 'test-project'");
        BuildResult buildResult = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.getRoot())
                .withDebug(true)
                .forwardOutput()
                .withArguments("clean")
                .build();
        assertTrue(buildResult.getOutput()
                .contains(
                        "Plugin is outdated in project test-project: org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]"));
        assertTrue(buildResult.getOutput()
                .contains(
                        "Plugin is outdated in project test-project: org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]@pom"));

        assertTrue(buildResult.getOutput()
                .contains(
                        "Plugin is outdated in project test-project: org.gradle:gradle-hello-world-plugin:[0.1 -> 0.2]:sources"));
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

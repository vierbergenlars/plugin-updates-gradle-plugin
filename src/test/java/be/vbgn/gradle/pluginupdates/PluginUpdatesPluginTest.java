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
                .build();

        assertTrue(buildResult.getOutput()
                .contains(
                        "Plugin is outdated in project test-project: org.gradle.hello-world:org.gradle.hello-world.gradle.plugin [0.1 -> 0.2]"));
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

package be.vbgn.gradle.pluginupdates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
abstract public class AbstractIntegrationTest {

    @Parameters(name = "Gradle v{0}")
    public static Collection<Object[]> testData() {
        List<Object[]> gradleVersions = Arrays.asList(new Object[][]{
                {"6.0.1"},
                {"5.6.4"},
                {"5.5.1"},
                {"5.4.1"},
                {"5.3.1"},
                {"5.2.1"},
                {"5.1.1"},
                {"5.0"},
                {"4.10.3"},
                {"4.9"},
                {"4.7"},
                {"4.6"},
                {"4.5.1"},
                {"4.4.1"}, // version shipped with debian stable
        });
        // Shuffle versions to avoid consistently getting blocked because all subclasses acquire a lock to download the same version
        Collections.shuffle(gradleVersions);
        return gradleVersions;
    }

    @Parameter(0)
    public String gradleVersion;

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private GradleRunner createGradleRunner(Path projectFolder, String task) throws IOException {
        FileUtils.copyDirectory(projectFolder.toFile(), testProjectDir.getRoot());
        return GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot().toPath().resolve("project").toFile())
                .withGradleVersion(gradleVersion)
                .withTestKitDir(testProjectDir.getRoot().toPath().resolve("gradleHome").toFile())
                .withArguments(task, "--stacktrace", "--rerun-tasks")
                .withDebug(true)
                .forwardOutput();

    }

    protected BuildResult buildProject(Path projectFolder, String task) throws IOException {
        return createGradleRunner(projectFolder, task).build();
    }

    protected BuildResult buildProjectAndFail(Path projectFolder, String task) throws IOException {
        return createGradleRunner(projectFolder, task).buildAndFail();
    }

    protected static void assertOutputContainsOneOf(BuildResult buildResult, String... messages) {
        boolean matched = false;
        StringBuilder assertMessage = new StringBuilder("\n");
        for (String expectedMessage : messages) {
            matched |= buildResult.getOutput().contains(expectedMessage);
            assertMessage.append(" * '").append(expectedMessage).append("'\n");
        }

        assertTrue("Build output: \n '''" + buildResult.getOutput() + "'''\n does not contain any of " + assertMessage,
                matched);
    }

    protected static void assertOutputContains(BuildResult buildResult, String message) {
        assertTrue("Build output: \n '''" + buildResult.getOutput() + "'''\n does not contain " + message,
                buildResult.getOutput().contains(message));

    }

    protected static void assertOutputNotContains(BuildResult buildResult, String message) {
        assertFalse("Build output: \n '''" + buildResult.getOutput() + "'''\n contains " + message,
                buildResult.getOutput().contains(message));

    }
}

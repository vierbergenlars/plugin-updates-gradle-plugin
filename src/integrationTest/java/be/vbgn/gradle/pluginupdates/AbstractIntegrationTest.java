package be.vbgn.gradle.pluginupdates;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
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

    @Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"4.10"}, {"4.4"}, {"4.3"}, {"4.0"}, {"3.2.1"}
        });
    }

    @Parameter(0)
    public String gradleVersion;

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    protected BuildResult buildProject(Path projectFolder, String task) throws IOException {
        FileUtils.copyDirectory(projectFolder.toFile(), testProjectDir.getRoot());
        System.out.print("Using gradle version " + gradleVersion);
        return GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot().toPath().resolve("project").toFile())
                .withGradleVersion(gradleVersion)
                .withTestKitDir(testProjectDir.getRoot().toPath().resolve("gradleHome").toFile())
                .withArguments(task, "--stacktrace", "--rerun-tasks")
                .withDebug(true)
                .forwardOutput()
                .build();
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
}

package be.vbgn.gradle.pluginupdates;

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
public class AbstractIntegrationTest {

    @Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"4.10"}, {"4.0"}, {"3.2.1"}
        });
    }

    @Parameter(0)
    private String gradleVersion;

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    protected BuildResult buildProject(Path projectFolder, String task) throws IOException {
        FileUtils.copyDirectory(projectFolder.toFile(), testProjectDir.getRoot());
        return GradleRunner.create()
                .withProjectDir(projectFolder.resolve("project").toFile())
                .withGradleVersion(gradleVersion)
                .withTestKitDir(projectFolder.resolve("gradleHome").toFile())
                .withArguments(task, "--stacktrace", "--rerun-tasks")
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
                .build();
    }

}

package be.vbgn.gradle.pluginupdates.update.finder.internal;

import static be.vbgn.gradle.pluginupdates.TestUtil.writeFile;
import static org.junit.Assume.assumeTrue;

import be.vbgn.gradle.pluginupdates.AbstractIntegrationTest;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.io.File;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class InvalidResolvesCacheTest extends AbstractIntegrationTest {

    private File buildFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
    }

    /**
     * https://github.com/vierbergenlars/plugin-updates-gradle-plugin/issues/4
     */
    @Test
    public void testOpenCacheMultiThreaded() throws IOException {
        assumeTrue("Gradle version is at least 4.3.0",
                Version.parse(gradleVersion).compareTo(Version.parse("4.3")) >= 0);
        writeFile(buildFile,
                "import be.vbgn.gradle.pluginupdates.update.finder.internal.InvalidResolvesCache\n"
                        + "import be.vbgn.gradle.pluginupdates.dsl.Util\n"
                        + "import org.gradle.cache.CacheRepository\n"
                        + "import java.util.concurrent.*\n"
                        + "plugins {\n"
                        + "id 'base'\n"
                        + "id 'be.vbgn.plugin-updates.config'"
                        + "}\n"
                        + "def cacheRepository = gradle.getServices().get(CacheRepository.class);\n"
                        + "def invalidResolvesCache = new InvalidResolvesCache(cacheRepository);\n"
                        + "def threadPool = Executors.newFixedThreadPool(80);\n"
                        + "def dependency = Util.createDependency('be.vbgn.gradle:test-dependency:1.2.3');"
                        + ""
                        + "task runThreadPool {\n"
                        + "doLast {\n"
                        + "for(def i = 0; i < 800; i++) {\n"
                        + "({j ->\n"
                        + "threadPool.execute({"
                        + "    println('Execute task '+j);\n"
                        + "    invalidResolvesCache.put(dependency);\n"
                        + "    Thread.sleep(1);\n"
                        + "    assert invalidResolvesCache.get(dependency) != null;\n"
                        + "    println('Done task '+j);\n"
                        + "})\n"
                        + "})(i);\n"
                        + "}\n"
                        + "threadPool.shutdown();\n"
                        + "threadPool.awaitTermination(10, TimeUnit.SECONDS);\n"
                        + "threadPool.shutdownNow();\n"
                        + "}\n"
                        + "}\n"
        );

        BuildResult buildResult = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.getRoot())
                .withGradleVersion(gradleVersion)
                .forwardOutput()
                .withArguments("runThreadPool")
                .build();

        assertOutputNotContains(buildResult, "Invalid resolves cache could not be opened.");
        for (int i = 0; i < 800; i++) {
            assertOutputContains(buildResult, "Execute task " + i);
            assertOutputContains(buildResult, "Done task " + i);
        }

    }

}

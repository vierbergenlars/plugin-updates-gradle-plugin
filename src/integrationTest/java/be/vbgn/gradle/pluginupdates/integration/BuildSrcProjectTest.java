package be.vbgn.gradle.pluginupdates.integration;

import static org.junit.Assert.assertFalse;

import be.vbgn.gradle.pluginupdates.AbstractIntegrationTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class BuildSrcProjectTest extends AbstractIntegrationTest {

    @Test
    public void buildSrcProject() throws IOException, URISyntaxException {
        BuildResult buildResult = buildProjectAndFail(Paths.get(getClass().getResource("buildSrc-project").toURI()),
                "clean");

        assertFalse(buildResult.getOutput().contains("The root project is not yet available for build"));
    }

}

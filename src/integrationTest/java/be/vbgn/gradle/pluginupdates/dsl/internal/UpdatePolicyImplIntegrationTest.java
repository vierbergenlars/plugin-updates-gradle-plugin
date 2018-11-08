package be.vbgn.gradle.pluginupdates.dsl.internal;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultVersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.junit.Test;

public class UpdatePolicyImplIntegrationTest {

    @Test
    public void ignoresAndRenames() {

        UpdatePolicyImpl updatePolicy = new UpdatePolicyImpl();
        updatePolicy.rename("eu.xenit.gradle:alfresco-sdk").to("org.gradle:gradle-hello-world-plugin");
        updatePolicy.ignore("org.gradle:gradle-hello-world-plugin").majorUpdates();

        VersionProvider versionProvider = updatePolicy.buildVersionProvider(new DefaultVersionProvider());

        UpdateFinder updateFinder = updatePolicy.buildUpdateFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                return versionProvider.getUpdateVersions(dependency)
                        .map(failureAllowedVersion -> dependency.withVersion(failureAllowedVersion.getVersion()));
            }
        });

        List<Dependency> dependencies = updateFinder.findUpdates(new DefaultDependency("eu.xenit.gradle", "alfresco-sdk", "0.1.3"))
                .collect(Collectors.toList());

        assertEquals(1, dependencies.size());
        assertEquals(new DefaultDependency("org.gradle", "gradle-hello-world-plugin", "+"), dependencies.get(0));
    }

}

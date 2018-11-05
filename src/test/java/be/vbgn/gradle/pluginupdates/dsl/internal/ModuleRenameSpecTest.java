package be.vbgn.gradle.pluginupdates.dsl.internal;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultModuleIdentifier;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.function.UnaryOperator;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.junit.Test;

public class ModuleRenameSpecTest {

    @Test
    public void toDependency() {
        ModuleRenameSpec moduleRenameSpec = new ModuleRenameSpec(new DefaultModuleIdentifier("be.vbgn.gradle", "test123"));

        Dependency transformTarget = new DefaultDependency("be.vbgn.gradle.test", "123", "1.2.3");

        moduleRenameSpec.to(transformTarget);

        UnaryOperator<Dependency> transformer = moduleRenameSpec.getTransformer();

        assertEquals(transformTarget, transformer.apply(new DefaultDependency("be.vbgn.gradle", "test123", "1.0.0")));
        assertEquals(transformTarget, transformer.apply(new DefaultDependency("be.vbgn.gradle", "test123", "4.5.6")));

        assertEquals(new DefaultDependency("be.vbgn.gradle", "test", "4.5.6"), transformer.apply(new DefaultDependency("be.vbgn.gradle", "test", "4.5.6")));
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test123", "4.5.6"), transformer.apply(new DefaultDependency("be.vbgn.gradle.test", "test123", "4.5.6")));
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test", "4.5.6"), transformer.apply(new DefaultDependency("be.vbgn.gradle.test", "test", "4.5.6")));
    }

    @Test
    public void toModule() {
        ModuleRenameSpec moduleRenameSpec = new ModuleRenameSpec(new DefaultModuleIdentifier("be.vbgn.gradle", "test123"));
        ModuleIdentifier transformTarget = new DefaultModuleIdentifier("be.vbgn.gradle.test", "123");

        moduleRenameSpec.to(transformTarget);

        UnaryOperator<Dependency> transformer = moduleRenameSpec.getTransformer();

        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "123", "1.0.0"), transformer.apply(new DefaultDependency("be.vbgn.gradle", "test123", "1.0.0")));

        assertEquals(new DefaultDependency("be.vbgn.gradle", "test", "4.5.6"), transformer.apply(new DefaultDependency("be.vbgn.gradle", "test", "4.5.6")));
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test123", "4.5.6"), transformer.apply(new DefaultDependency("be.vbgn.gradle.test", "test123", "4.5.6")));
        assertEquals(new DefaultDependency("be.vbgn.gradle.test", "test", "4.5.6"), transformer.apply(new DefaultDependency("be.vbgn.gradle.test", "test", "4.5.6")));
    }

    @Test(expected = IllegalStateException.class)
    public void toNothing() {
        ModuleRenameSpec moduleRenameSpec = new ModuleRenameSpec(new DefaultModuleIdentifier("be.vbgn.gradle", "test123"));

        moduleRenameSpec.getTransformer();
    }
}

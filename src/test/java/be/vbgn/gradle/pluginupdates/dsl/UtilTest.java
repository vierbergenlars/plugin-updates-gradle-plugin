package be.vbgn.gradle.pluginupdates.dsl;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.HashMap;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.junit.Test;

public class UtilTest {

    @Test
    public void createModuleIdentifierString() {
        ModuleIdentifier moduleIdentifier = Util.createModuleIdentifier("be.vbgn.gradle:test");

        assertEquals("be.vbgn.gradle", moduleIdentifier.getGroup());
        assertEquals("test", moduleIdentifier.getName());
    }

    @Test(expected = BadNotationException.class)
    public void createModuleIdentifierStringInvalid() {
        Util.createModuleIdentifier("be.vbgn.gradle:test:123");
    }

    @Test(expected = BadNotationException.class)
    public void createModuleIdentifierStringInvalid2() {
        Util.createModuleIdentifier("be.vbgn.gradle");
    }

    @Test
    public void createModuleIdentifierMap() {
        HashMap<String, String> identifier = new HashMap<>();

        identifier.put("group", "be.vbgn.gradle");
        identifier.put("name", "test");
        ModuleIdentifier moduleIdentifier = Util.createModuleIdentifier(identifier);

        assertEquals("be.vbgn.gradle", moduleIdentifier.getGroup());
        assertEquals("test", moduleIdentifier.getName());
    }

    @Test(expected = BadNotationException.class)
    public void createModuleIdentifierMapInvalid() {
        HashMap<String, String> identifier = new HashMap<>();

        identifier.put("group", "be.vbgn.gradle");
        Util.createModuleIdentifier(identifier);
    }

    @Test(expected = BadNotationException.class)
    public void createModuleIdentifierMapInvalid2() {
        HashMap<String, String> identifier = new HashMap<>();

        identifier.put("group", "be.vbgn.gradle");
        identifier.put("name", "test");
        identifier.put("version", "1.2.3");
        Util.createModuleIdentifier(identifier);
    }

    @Test
    public void createDependencyString() {
        Dependency dependency = Util.createDependency("be.vbgn.gradle:test:1.2.3");

        assertEquals("be.vbgn.gradle", dependency.getGroup());
        assertEquals("test", dependency.getName());
        assertEquals(Version.parse("1.2.3"), dependency.getVersion());
    }

    @Test(expected = BadNotationException.class)
    public void createDependencyStringInvalid() {
        Util.createDependency("be.vbgn.gradle:test");
    }

    @Test(expected = BadNotationException.class)
    public void createDependencyStringInvalid2() {
        Util.createDependency("be.vbgn.gradle:test:12.3:bla");
    }

    @Test
    public void createDependencyMap() {
        HashMap<String, String> identifier = new HashMap<>();
        identifier.put("group", "be.vbgn.gradle");
        identifier.put("name", "test");
        identifier.put("version", "1.2.3");
        Dependency dependency = Util.createDependency(identifier);

        assertEquals("be.vbgn.gradle", dependency.getGroup());
        assertEquals("test", dependency.getName());
        assertEquals(Version.parse("1.2.3"), dependency.getVersion());
    }

    @Test(expected = BadNotationException.class)
    public void createDependencyMapInvalid() {
        HashMap<String, String> identifier = new HashMap<>();
        identifier.put("group", "be.vbgn.gradle");
        identifier.put("name", "test");
        Util.createDependency(identifier);
    }

    @Test(expected = BadNotationException.class)
    public void createDependencyMapInvalid2() {
        HashMap<String, String> identifier = new HashMap<>();
        identifier.put("group", "be.vbgn.gradle");
        identifier.put("name", "test");
        identifier.put("version", "1.2.3");
        identifier.put("bla", "zyz");
        Util.createDependency(identifier);
    }
}

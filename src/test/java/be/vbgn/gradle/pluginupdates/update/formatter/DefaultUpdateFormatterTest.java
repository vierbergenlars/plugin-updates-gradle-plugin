package be.vbgn.gradle.pluginupdates.update.formatter;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.Update;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

public class DefaultUpdateFormatterTest {

    private static class UpdateImpl implements Update {

        private Dependency original;
        private List<Dependency> updates;

        public UpdateImpl(Dependency original, List<Dependency> updates) {
            this.original = original;
            this.updates = updates;
        }

        @Override
        public Dependency getOriginal() {
            return original;
        }

        @Override
        public List<Dependency> getUpdates() {
            return updates;
        }
    }

    @Test
    public void formatChangedVersion() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.test", "test1", "0.5.6");
        List<Dependency> updates = new LinkedList<>();
        updates.add(original.withVersion("0.5.7"));
        Update update = new UpdateImpl(original, updates);

        UpdateFormatter formatter = new DefaultUpdateFormatter();

        assertEquals("be.vbgn.test:test1:[0.5.6 -> 0.5.7]", formatter.format(update));

        updates.add(original.withVersion("1.0.0"));
        assertEquals("be.vbgn.test:test1:[0.5.6 -> 0.5.7 -> 1.0.0]", formatter.format(update));
    }

    @Test
    public void formatChangedName() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.test", "test1", "0.5.6");
        List<Dependency> updates = new LinkedList<>();
        updates.add(original.withName("test2"));
        Update update = new UpdateImpl(original, updates);

        UpdateFormatter formatter = new DefaultUpdateFormatter();

        assertEquals("be.vbgn.test:[test1:0.5.6 -> test2:0.5.6]", formatter.format(update));

        updates.add(original.withVersion("1.0.0"));
        assertEquals("be.vbgn.test:[test1:0.5.6 -> test2:0.5.6 -> test1:1.0.0]", formatter.format(update));
    }

    @Test
    public void formatChangedGroup() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.test", "test1", "0.5.6");
        List<Dependency> updates = new LinkedList<>();
        updates.add(original.withName("test2").withGroup("be.vbgn.gradle").withVersion("0.5.7"));
        Update update = new UpdateImpl(original, updates);

        UpdateFormatter formatter = new DefaultUpdateFormatter();

        assertEquals("[be.vbgn.test:test1:0.5.6 -> be.vbgn.gradle:test2:0.5.7]", formatter.format(update));

        updates.add(original.withVersion("1.0.0").withName("test2").withGroup("be.vbgn.gradle"));
        assertEquals("[be.vbgn.test:test1:0.5.6 -> be.vbgn.gradle:test2:0.5.7 -> be.vbgn.gradle:test2:1.0.0]",
                formatter.format(update));
    }

}

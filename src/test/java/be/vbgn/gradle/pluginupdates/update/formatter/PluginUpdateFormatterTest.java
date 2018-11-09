package be.vbgn.gradle.pluginupdates.update.formatter;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.Update;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.Test;

public class PluginUpdateFormatterTest {
    private static class UpdateImpl implements Update {

        private Dependency original;
        private List<Dependency> updates;

        public UpdateImpl(Dependency original, List<Dependency> updates) {
            this.original = original;
            this.updates = updates;
        }

        @Nonnull
        @Override
        public Dependency getOriginal() {
            return original;
        }

        @Nonnull
        @Override
        public List<Dependency> getUpdates() {
            return updates;
        }
    }

    private static final UpdateFormatter BROKEN_FORMATTER = new UpdateFormatter() {
            @Nonnull
            @Override
            public String format(@Nonnull Update update) {
                throw new UnsupportedOperationException();
            }
        };

    @Test
    public void formatChangedVersion() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.test", "be.vbgn.test.gradle.plugin", "0.5.6");
        List<Dependency> updates = new LinkedList<>();
        updates.add(original.withVersion("0.5.7"));
        Update update = new UpdateImpl(original, updates);

        UpdateFormatter formatter = new PluginUpdateFormatter(BROKEN_FORMATTER);

        assertEquals("id 'be.vbgn.test' version '[0.5.6 -> 0.5.7]'", formatter.format(update));

        updates.add(original.withVersion("1.0.0"));
        assertEquals("id 'be.vbgn.test' version '[0.5.6 -> 0.5.7 -> 1.0.0]'", formatter.format(update));
    }

    @Test
    public void formatChangedName() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.test", "be.vbgn.test.gradle.plugin", "0.5.6");
        List<Dependency> updates = new LinkedList<>();
        updates.add(original.withGroup("be.vbgn.test2").withName("be.vbgn.test2.gradle.plugin"));
        Update update = new UpdateImpl(original, updates);

        UpdateFormatter formatter = new PluginUpdateFormatter(BROKEN_FORMATTER);

        assertEquals("[id 'be.vbgn.test' version '0.5.6' -> id 'be.vbgn.test2' version '0.5.6']", formatter.format(update));

        updates.add(original.withVersion("1.0.0"));
        assertEquals("[id 'be.vbgn.test' version '0.5.6' -> id 'be.vbgn.test2' version '0.5.6' -> id 'be.vbgn.test' version '1.0.0']", formatter.format(update));
    }

    @Test
    public void formatChangedGroupDelegates() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.test", "test1", "0.5.6");
        List<Dependency> updates = new LinkedList<>();
        updates.add(original.withName("test2").withGroup("be.vbgn.gradle").withVersion("0.5.7"));
        Update update = new UpdateImpl(original, updates);

        UpdateFormatter formatter = new PluginUpdateFormatter(new UpdateFormatter() {
            @Nonnull
            @Override
            public String format(@Nonnull Update update2) {
                assertEquals(update, update2);
                return "bla";
            }
        });

        assertEquals("bla", formatter.format(update));
    }

    @Test
    public void formatRepeatingChange() {
        Dependency original = new DefaultDependency("be.vbgn.test", "be.vbgn.test.gradle.plugin", "0.5.6");
        List<Dependency> updates = new LinkedList<>();
        updates.add(original);
        updates.add(original.withVersion("0.6.0"));
        updates.add(original.withVersion("0.6.0"));
        updates.add(original.withVersion("1.0.0"));
        Update update = new UpdateImpl(original, updates);

        UpdateFormatter formatter = new PluginUpdateFormatter(BROKEN_FORMATTER);

        assertEquals("id 'be.vbgn.test' version '[0.5.6 -> 0.6.0 -> 1.0.0]'", formatter.format(update));
    }


}

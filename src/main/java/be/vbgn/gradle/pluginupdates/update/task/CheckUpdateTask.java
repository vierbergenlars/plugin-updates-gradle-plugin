package be.vbgn.gradle.pluginupdates.update.task;

import be.vbgn.gradle.pluginupdates.update.Update;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.cache.CacheRepository;

public class CheckUpdateTask extends DefaultTask {

    private final Property<Configuration> configuration = getProject().getObjects()
            .property(Configuration.class);

    private final ListProperty<Update> updates = getProject().getObjects().listProperty(Update.class);

    private final UpdateChecker updateChecker;

    @Inject
    public CheckUpdateTask(CacheRepository cacheRepository) {
        updateChecker = new UpdateChecker(cacheRepository, new ConfigurationCollector(getProject().getGradle()));
    }


    @Input
    public Property<Configuration> getConfiguration() {
        return configuration;
    }

    @Internal
    public ListProperty<Update> getUpdates() {
        return updates;
    }

    @TaskAction
    public void findUpdates() {
        getUpdates().set(updateChecker.getUpdates(getProject(), configuration.get()));
    }
}

package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dsl.UpdateCheckerConfiguration;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

public class UpdateCheckerConfigurationImpl implements UpdateCheckerConfiguration, Serializable {

    private UpdateBuilder policy = new UpdatePolicyImpl();

    public UpdateCheckerConfigurationImpl() {

    }

    public UpdateCheckerConfigurationImpl(UpdateBuilder policy) {
        this.policy = policy;
    }

    @Nonnull
    public UpdateBuilder getPolicy() {
        return policy;
    }

    @Nonnull
    public static UpdateCheckerConfigurationImpl merge(@Nonnull UpdateCheckerConfigurationImpl... configurations) {
        UpdateCheckerConfigurationImpl configuration = new UpdateCheckerConfigurationImpl();

        List<UpdateBuilder> updateBuilders = new LinkedList<>();

        for (UpdateCheckerConfigurationImpl updateCheckerConfiguration : configurations) {
            updateBuilders.add(updateCheckerConfiguration.policy);
        }

        configuration.policy = new MergedUpdatePolicyImpl(updateBuilders);
        return configuration;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(policy);

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        policy = (UpdateBuilder) in.readObject();

    }

    private void readObjectNoData()
            throws ObjectStreamException {
        policy = new UpdatePolicyImpl();
    }
}

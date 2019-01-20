package be.vbgn.gradle.pluginupdates.update.resolver.internal;

public class InvalidResolvesMemoryCacheTest extends AbstractResolvesMemoryCacheTest {

    @Override
    protected InvalidResolvesCache createInvalidResolvesCache() {
        return new InvalidResolvesMemoryCache();
    }
}

# Plugin updates plugin [![CI](https://github.com/vierbergenlars/plugin-updates-gradle-plugin/workflows/CI/badge.svg)](https://github.com/vierbergenlars/plugin-updates-gradle-plugin/actions?query=workflow%3ACI+branch%3Amaster)
 [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=vierbergenlars_plugin-updates-gradle-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=vierbergenlars_plugin-updates-gradle-plugin)

A gradle plugin that checks for updates of your gradle plugins.

## Installation

### As initscript plugin

If you want to check for plugin updates across all your projects, you can install this plugin as an initscript plugin.

Plugins in all projects will automatically be checked for updates.

To use this plugin for all projects, you can add the following snippet in a file in your global gradle `init.d` folder.
```gradle
initscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath 'be.vbgn.gradle:plugin-updates-plugin:+'
    }
}
apply plugin: be.vbgn.gradle.pluginupdates.PluginUpdatesPlugin
```

On linux, your global gradle `init.d` folder is located in `~/.gradle/init.d/`.
On windows, your global gradle `init.d` folder is located in your user folder, as `.gradle/init.d`.

### As buildscript plugin

If you want toch check for plugin updates in a single project, you can use the buildscript (normal) plugin.

Update checks will only apply to the project where this plugin is applied, subprojects are not checked automatically.

Add to the top of `build.gradle` in your project:

```gradle
buildscript {
    dependencies {
        classpath 'be.vbgn.gradle:plugin-updates-plugin:+'
    }
}
apply plugin: be.vbgn.gradle.pluginupdates.PluginUpdatesPlugin
```

### As settings plugin

If you want to check for updates for all projects of a multi-project build, and don't want to have to apply the plugin separately on every project,
you can use the settings plugin.

Update checks will apply to all projects of your multi-project build.

Add to the top of `settings.gradle` in your project:

```gradle
buildscript {
    dependencies {
        classpath 'be.vbgn.gradle:plugin-updates-plugin:+'
    }
}
apply plugin: be.vbgn.gradle.pluginupdates.PluginUpdatesPlugin
```


## Usage

After every build, the plugin will check for updates to your buildscript classpath dependencies.

If a newer versions of any plugin is found, a warning is printed after the build output, similar to the following:
`Plugin is outdated in project docker-image: eu.xenit.docker-alfresco:eu.xenit.docker-alfresco.gradle.plugin [4.0.0 -> 4.0.2]`

You can then decide to update the plugin, or to ignore the warning.

## Configuration

You can configure an update notification policy for your plugins.
In this policy, you can ignore certain updates for some plugins (for example, ignore new major versions that will have breaking changes)
and you can indicate that a plugin has changed their group/name.

The plugin updates plugin can be configured with an extension provided by the configuration plugin `be.vbgn.gradle.pluginupdates.ConfigurationPlugin`.
This configuration plugin is applied automatically by the main plugin updates plugin and can be used in an initscript, settings script or a buildscript.

But it can also be applied separately to configure the plugin from a place where you do not want to always activate

For example, you can provide a custom plugin updates policy for a multi-project build by adding the configuration to `settings.gradle`.
By not applying the main plugin updates plugin, a user can choose himself if they want to get plugin update notifications by applying the
main plugin in their initscript.

```gradle
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath 'be.vbgn.gradle:plugin-updates-plugin:+'
    }
}
apply plugin: be.vbgn.gradle.pluginupdates.ConfigurationPlugin

pluginUpdates {
    policy {
        // Change the group and name of a plugin. Any version of the eu.xenit.gradle:alfresco-docker-plugin will be
        // suggested as a replacement of any version of eu.xenit.gradle:xenit-gradle-plugins
        rename "eu.xenit.gradle:xenit-gradle-plugins" to "eu.xenit.gradle:alfresco-docker-plugin"

        // Change the group and name of a plugin. Any version >= 0.1.4 of eu.xenit.gradle:alfresco-sdk will be
        // suggested as a replacement of any version of eu.xenit.gradle.plugins:ampde_gradle_plugin
        rename "eu.xenit.gradle.plugins:ampde_gradle_plugin" to "eu.xenit.gradle:alfresco-sdk:0.1.4+"

        // Ignore a certain level of updates for a plugin
        ignore "gradle.plugin.com.github.eerohele:saxon-gradle" majorUpdates() // Ignore major updates for a plugin
        ignore "eu.xenit.gradle:alfresco-sdk" minorUpdates() // Ignore minor updates for a plugin
        ignore "org.springframework.boot:spring-boot-gradle-plugin" microUpdates() // Ignore micro updates for a plugin

        // Ignore a certain version from updates for a plugin
        ignore "eu.xenit.gradle:alfresco-sdk:0.1.3" because "It contains a critical bug"

        // Ignore all updates for a plugin
        ignore "be.vbgn.gradle:plugin-updates-plugin" because "We don't ever want to update this plugin"
    }
}
```

# Development

## Creating a release

Every git tag is automatically published to the gradle plugins repository by Travis-CI.

This plugin follows SemVer and tags are managed with Reckon.

To create a release from a commit, use `./gradlew reckonTagPush -Preckon.scope=patch -Preckon.stage=final` to create a new patch release.

Tests are required to pass before a new release can be tagged.

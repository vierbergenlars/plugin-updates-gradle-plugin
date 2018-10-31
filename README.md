# Plugin updates plugin [![Build Status](https://travis-ci.org/vierbergenlars/plugin-updates-gradle-plugin.svg?branch=master)](https://travis-ci.org/vierbergenlars/plugin-updates-gradle-plugin)

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

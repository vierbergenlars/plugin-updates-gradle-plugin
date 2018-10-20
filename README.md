# Plugin updates plugin

A gradle plugin that checks for updates of your gradle plugins.

## Installation

### As initscript plugin

This plugin can be installed as an initscript plugin.

All projects will be automatically enrolled in update checking.

#### Linux

Open the file `~/.gradle/init.gradle`, or create it if it does not exist.
Add this block at the start of the file:

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

### As buildscript plugin

If you only want to apply the plugin for a single project, you can apply it as a buildscript (normal) plugin

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

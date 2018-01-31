Gradle Cbt Python Envs Plugin
=============================

Gradle plugin to create Python envs.

This plugin is based on [gradle-python-envs](https://github.com/JetBrains/gradle-python-envs),

1. A convenient DSL to specify target Python environments created by the cbt build system
2. Package installation for Windows, Osx and Linux x86-64

Usage
-----
                                                
Apply the plugin to your project following
[`https://plugins.gradle.org/plugin/com.h1grid.cbt.python.envs`](https://plugins.gradle.org/plugin/com.h1grid.cbt.python.envs),
and configure the extension:

```gradle
envs {

    bootstrapDirectory = new File(buildDir, 'bootstrap')

//  defaultUrlBase = "https://h1grid.com/artifactory/cbt/tools"
//  defaultPackageName = "python2"
//  defaultPackageVersion = "2.7.14.49.re579c52b"

    python "2.7.14.49.re579c52b"
    python ["django==1.9"]
    python "2.7.14.49.re579c52b", ["django==1.9"]
}
```

Then invoke the `build_envs` task. 

This will download and install specified python's interpreters to `buildDir/bootstrap`.

Libraries listed will be installed correspondingly. Packages in list are installed with `pip install` command. 
It enables to install, for example, PyQt in env.

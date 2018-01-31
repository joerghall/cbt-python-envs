// MIT License
//
// Copyright (c) 2018 Joerg Hallmann
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
// https://github.com/joerghall/cbt-python-envs
//
package com.h1grid.cbt.python.envs

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.internal.os.OperatingSystem

/**
 * Project extension to configure Python build environment.
 *
 */
class PythonEnvsExtension {

    File bootstrapDirectory

    URL defaultUrlBase = new URL("https://h1grid.com/artifactory/cbt/tools")
    String defaultPackageName = "python2"
    String defaultPackageVersion = "2.7.14.49.re579c52b"

    List<Python> pythons = []

    /**
     * @param version py version like "2.7.14.49.re579c52b"
     * @param packages collection of py packages to install
     */
    Python python(final String packageVersion,
                final List<Python> packages) {
        if (bootstrapDirectory==null) {
            throw new Exception("bootstrapDirectory is not defined")
        }
        pythons << new Python(defaultPackageName, bootstrapDirectory, EnvType.PYTHON, packageVersion, packages, defaultUrlBase)
        pythons.last()
    }

    /**
     * @param packages collection of py packages to install
     */
    Python python(final List<Python> packages) {
        python(defaultPackageVersion, packages)
    }

    /**
     * pyton based on defaults
     */
    Python python() {
        python(defaultPackageVersion, null)
    }
}

enum EnvType {
    PYTHON
    static fromString(String type) {
        return type == null ? null : valueOf(type.toUpperCase())
    }
}

class Python {
    String packageName
    final File envDir
    final EnvType type
    final String packageVersion
    final List<String> packages
    URL urlBase
    URL url
    String packageFile
    String relPath
    String osName = OsName()

    Python(String name,
           File dir,
           EnvType type,
           String version,
           List<String> packages,
           URL urlBase) {

        this.packageName = name
        this.envDir = new File(dir, name)
        this.type = type
        this.packageVersion = version
        this.packages = packages
        this.urlBase = urlBase
    }

    String getPackageFile() {
        if (packageFile==null) {
            return "${packageName}-${packageVersion}-${osName}.tgz"
        } else {
            return packageFile
        }
    }

    URL getUrl() {
        if(url == null) {
            return new URL("${urlBase}/${packageName}/${packageVersion}/${getPackageFile()}")
        } else {
            return url
        }
    }

    String getRelPath() {
        if(relPath == null) {
            return "${packageName}/${packageVersion}/${osName}"
        } else {
            return relPath
        }
    }

    private static Boolean isWindows = Os.isFamily(Os.FAMILY_WINDOWS)

    File getExecutable(String executable) {
        String pathString

        switch (type) {
            case EnvType.PYTHON:
                if (executable in ["pip", "virtualenv"]) {
                    pathString = isWindows ? "${getRelPath()}/${executable}.exe" : "${getRelPath()}/bin/${executable}"
                } else if (executable.startsWith("python")) {
                    pathString = isWindows ? "${getRelPath()}/${executable}.exe" : "${getRelPath()}/bin/${executable}"
                } else {
                    throw new RuntimeException("$executable is not supported for $type yet")
                }
                break
            default:
                throw new RuntimeException("$type env type is not supported yet")
        }

        return new File(envDir, pathString)
    }

    File executable() {
        File f = getExecutable("python")
        if (!f.exists()) {
            throw new RuntimeException("${f.toString()} doesn't exists")
        }
        return f
    }

    private static String OsName() {
        if(Os.isFamily(Os.FAMILY_WINDOWS))
            return "windows"
        else if(Os.isFamily(Os.FAMILY_MAC))
            return "osx"
        else if(OperatingSystem.current().isLinux())
            return "linux"
    }
}

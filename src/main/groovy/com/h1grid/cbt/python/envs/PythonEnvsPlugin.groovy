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
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.text.SimpleDateFormat

class PythonEnvsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.mkdir("build")
        PythonEnvsExtension envs = project.extensions.create("envs", PythonEnvsExtension.class)

        project.repositories {
            mavenCentral()
        }

        project.afterEvaluate {

            Task python_from_tgz_task = project.tasks.create(name: 'build_pythons_from_tgz') {

                onlyIf { !envs.pythons.empty }

                envs.pythons.each { env ->
                    dependsOn project.tasks.create(name: "Bootstrap ${env.type ?: ''} '$env.packageName' from archive") {
                        onlyIf {
                            !(new File( new File(env.envDir, env.relPath), "cbt_report.cvs")).exists()
                        }

                        doLast {
                            getPackage(project, env)

                            pipInstall(project, env, env.packages)
                        }
                    }
                }
            }
        }
    }

    private static File getExecutable(String executable, Python env) {
        return env.getExecutable(executable)
    }

    private static File getPipFile(Project project) {
        new File(project.buildDir, "get-pip.py").with { file ->
            if (!file.exists()) {
                project.ant.get(dest: file) {
                    url(url: "https://bootstrap.pypa.io/get-pip.py")
                }
            }
            return file
        }
    }

    private void pipInstall(Project project, Python env, List<String> packages) {
        if (packages == null || packages.empty) {
            return
        }

        project.logger.quiet("Installing packages via pip: $packages")

        List<String> command = [
                getExecutable("pip", env),
                "install",
                *project.extensions.findByName("envs").getProperty("pipInstallOptions").split(" "),
                *packages
        ]
        project.logger.quiet("Executing '${command.join(" ")}'")

        if (project.exec {
            commandLine command
        }.exitValue != 0) throw new GradleException("pip install failed")

    }

    private void getPackage(Project project, Python env) {
        try {
            String archiveName = env.url.toString().with { urlString ->
                urlString.substring(urlString.lastIndexOf('/') + 1, urlString.length())
            }
            if (!archiveName.endsWith("tgz") && !archiveName.endsWith("tar.gz")) {
                throw new RuntimeException("Wrong archive extension, only tgz is supported")
            }

            File archive = new File(project.buildDir, env.getPackageFile())
            project.logger.quiet("Downloading $archiveName archive from $env.url to ${archive}")
            project.ant.get(dest: archive) {
                url(url: env.url)
            }

            project.logger.quiet("Unpacking downloaded ${archive} archive to ${env.envDir}")
            project.ant.untar(src: archive, dest: env.envDir, compression: "gzip")
            if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
                project.exec {
                    executable "bash"
                    args "-c", "chmod +x ${env.envDir}/${env.getRelPath()}/bin/*"
                }
            }

            new File("${env.envDir}/${env.getRelPath()}/cbt_report.cvs").text =
                    new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())

            if (env.type != null) {
                if (!env.getExecutable("pip").exists()) {
                    project.logger.quiet("Downloading & installing pip and setuptools")
                    project.exec {
                        executable env.getExecutable("python")
                        args getPipFile(project)
                    }
                } else {
                    project.logger.quiet("Force upgrade pip and setuptools")
                    project.exec {
                        executable env.getExecutable("python")
                        args "-m", "pip", "install", "--upgrade", "--force", "setuptools", "pip"
                    }
                }
            }

            project.logger.quiet("Deleting $archiveName archive")
            archive.delete()
        }
        catch (Exception e) {
            project.logger.error(e.message)
            throw new GradleException(e.message)
        }
    }


}

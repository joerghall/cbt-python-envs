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

import org.junit.Rule
import org.junit.rules.TemporaryFolder

class PythonEnvsPluginUnitTest extends GroovyTestCase {

    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()


    void testMissingBoostrapDirectoryMessage() {
        def msg = shouldFail(Exception) {
            def extension = new PythonEnvsExtension()
            extension.python()
        }
        assertEquals(msg, "bootstrapDirectory is not defined")
    }

    void testDefault() {
        testProjectDir.create()

        def extension = new PythonEnvsExtension()

        extension.bootstrapDirectory = testProjectDir.getRoot()

        def python = extension.python()

        assertEquals(python.getPackageFile(), "python2-2.7.14.49.re579c52b-${python.osName}.tgz")
        assertEquals(python.getUrl().toString(), "https://h1grid.com/artifactory/cbt/tools/python2/2.7.14.49.re579c52b/python2-2.7.14.49.re579c52b-${python.osName}.tgz")
        assertEquals(python.getRelPath(), "python2/2.7.14.49.re579c52b/${python.osName}")
    }

    void testModifiedDefaults() {
        testProjectDir.create()

        def extension = new PythonEnvsExtension()

        extension.bootstrapDirectory = testProjectDir.getRoot()
        extension.defaultUrlBase = new URL("http://h1grid.com/artifactory/cbt/tools1")
        extension.defaultPackageName = "python"
        extension.defaultPackageVersion = "2.7.14"

        def python = extension.python()

        assertEquals(python.getPackageFile(), "python-2.7.14-${python.osName}.tgz")
        assertEquals(python.getUrl().toString(), "http://h1grid.com/artifactory/cbt/tools1/python/2.7.14/python-2.7.14-${python.osName}.tgz")
        assertEquals(python.getRelPath(), "python/2.7.14/${python.osName}")
    }

    void testOverwriteRelPat() {
        testProjectDir.create()

        def extension = new PythonEnvsExtension()

        extension.bootstrapDirectory = testProjectDir.getRoot()

        def python = extension.python()

        python.relPath = "python"

        assertEquals(python.getPackageFile(), "python2-2.7.14.49.re579c52b-${python.osName}.tgz")
        assertEquals(python.getUrl().toString(), "https://h1grid.com/artifactory/cbt/tools/python2/2.7.14.49.re579c52b/python2-2.7.14.49.re579c52b-${python.osName}.tgz")
        assertEquals(python.getRelPath(), "python")
    }

    void testOverwriteOsName() {
        testProjectDir.create()

        def extension = new PythonEnvsExtension()

        extension.bootstrapDirectory = testProjectDir.getRoot()

        def python = extension.python()

        python.osName = "unknown"

        assertEquals(python.getPackageFile(), "python2-2.7.14.49.re579c52b-unknown.tgz")
        assertEquals(python.getUrl().toString(), "https://h1grid.com/artifactory/cbt/tools/python2/2.7.14.49.re579c52b/python2-2.7.14.49.re579c52b-unknown.tgz")
        assertEquals(python.getRelPath(), "python2/2.7.14.49.re579c52b/unknown")
    }

    void testOverwritePackageFile() {
        testProjectDir.create()

        def extension = new PythonEnvsExtension()

        extension.bootstrapDirectory = testProjectDir.getRoot()

        def python = extension.python()

        python.packageFile = "python.tgz"

        assertEquals(python.getPackageFile(), "python.tgz")
        assertEquals(python.getUrl().toString(), "https://h1grid.com/artifactory/cbt/tools/python2/2.7.14.49.re579c52b/python.tgz")
        assertEquals(python.getRelPath(), "python2/2.7.14.49.re579c52b/${python.osName}")
    }

    void testOverwriteUrl() {
        testProjectDir.create()

        def extension = new PythonEnvsExtension()

        extension.bootstrapDirectory = testProjectDir.getRoot()

        def python = extension.python()

        python.url = new URL("https://h1grid.com/artifactory/cbt/tools/python2/2.7.14.latest/python2-2.7.14.49.re579c52b-${python.osName}.tgz")

        assertEquals(python.getPackageFile(), "python2-2.7.14.49.re579c52b-${python.osName}.tgz")
        assertEquals(python.getUrl().toString(), "https://h1grid.com/artifactory/cbt/tools/python2/2.7.14.latest/python2-2.7.14.49.re579c52b-${python.osName}.tgz")
        assertEquals(python.getRelPath(), "python2/2.7.14.49.re579c52b/${python.osName}")
    }
}

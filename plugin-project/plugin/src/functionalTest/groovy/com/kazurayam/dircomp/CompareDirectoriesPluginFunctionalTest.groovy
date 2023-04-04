package com.kazurayam.dircomp

import spock.lang.Specification
import spock.lang.TempDir
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CompareDirectoriesPluginFunctionalTest extends Specification {

    // fields
    @TempDir
    private Path tempDir

    private Path projectDir
    private Path fixturesDir
    private Path outputFile
    private Path diffDir

    // fixture methods
    def setupSpec() {
        projectDir = Paths.get(".").toAbsolutePath().normalize()
        fixturesDir = projectDir.resolve("src/test/fixtures").toAbsolutePath()
    }

    def setup() {
        outputFile = tempDir.resolve( "build/tmp/differences.json").toAbsolutePath()
        diffDir = tempDir.resolve("build/tmp/diff").toAbsolutePath()
        settingsFile << ""
        buildFile << """
plugins {
    id('com.kazurayam.compareDirectories')
}
compareDirectories {
    dirA = layout.projectDirectory.dir("${fixturesDir.toString()}/A")
    dirB = layout.projectDirectory.dir("${fixturesDir.toString()}/B")
    outputFile = layout.buildDirectory.file("tmp/differences.json")
    diffDir = layout.buildDirectory.dir("tmp/diff")
}
"""
    }
    //def cleanup() {}
    //def cleanupSpec() {}


    // feature methods
    def "can run task"() {
        given:
        assert Files.exists(fixturesDir)
        println "fixturesDir=${fixturesDir.toString()}"
        Files.createDirectories(tempDir.resolve("build"))

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("compareDirectories")
        runner.withProjectDir(tempDir)
        def result = runner.build()
        String message = outputFile.toFile().text
        println "[CompareDirectoriesPluginFunctionalTest]"
        println message

        then:
        message.contains("filesOnlyInA")
        message.contains("filesOnlyInB")
        message.contains("intersection")
        message.contains("modifiedFiles")
    }


    // helper methods
    private Path getBuildFile() {
        return tempDir.resolve("build.gradle")
    }

    private Path getSettingsFile() {
        return tempDir.resolve("settings.gradle")
    }
}

package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification
import spock.lang.TempDir
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CompareDirectoriesPluginFunctionalTest extends Specification {

    private static TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(CompareDirectoriesPluginFunctionalTest.class)
                    .subDirPath(CompareDirectoriesPluginFunctionalTest.class).build()

    private static Path fixturesDir
    private Path outputFile
    private Path diffDir

    // fixture methods
    def setupSpec() {
        too.cleanClassOutputDirectory()
        fixturesDir = too.getProjectDir().resolve("src/test/fixtures").toAbsolutePath()
        Path dataDir = too.getClassOutputDirectory().resolve("data")
        too.copyDir(fixturesDir, dataDir)
    }

    def setup() {
        settingsFile << ""
        buildFile << """
plugins {
    id("com.kazurayam.compare-directories") version "0.2.3"
}

compareDirectories {
    dirA = fileTree(layout.projectDirectory.dir("src/test/fixtures/A")) { exclude "**/*.png" }
    dirB = fileTree(layout.projectDirectory.dir("src/test/fixtures/B")) { exclude "**/*.png" }
    outputFile = layout.buildDirectory.file("tmp/differences.json")
    diffDir = layout.buildDirectory.dir("tmp/diff")
}

tasks.register("dircomp", com.kazurayam.dircomp.CompareDirectoriesTask) {
    dirA = fileTree(layout.projectDirectory.dir("src/test/fixtures/A")) { exclude "**/*.png" }
    dirB = fileTree(layout.projectDirectory.dir("src/test/fixtures/B")) { exclude "**/*.png" }
    outputFile = layout.buildDirectory.file("tmp/differences.json")
    diffDir = layout.buildDirectory.dir("tmp/diff")
    doFirst {
        delete layout.buildDirectory.dir("tmp")
    }
    doLast {
        println "output at " + layout.buildDirectory.dir("tmp").get()
    }
}
"""
        outputFile = too.getClassOutputDirectory().resolve( "build/out/differences.json").toAbsolutePath()
        diffDir = too.getClassOutputDirectory().resolve("build/out/diff").toAbsolutePath()

        println '=============================================================='
        Files.readAllLines(buildFile).eachWithIndex { line, index ->
            println "${index+1} ${line}"
        }
        println '=============================================================='
    }
    //def cleanup() {}
    //def cleanupSpec() {}

    // feature methods
    def "can run compareDirectories task"() {
        given:
        assert Files.exists(fixturesDir)
        println "fixturesDir=${fixturesDir.toString()}"
        Files.createDirectories(too.getClassOutputDirectory().resolve("build"))

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(too.getClassOutputDirectory().toFile())
                .withArguments("compareDirectories")
                .withPluginClasspath()
                .build()
        String message = outputFile.toFile().text
        println message

        then:
        message.contains("filesOnlyInA")
        message.contains("filesOnlyInB")
        message.contains("intersection")
        message.contains("modifiedFiles")
        !message.contains("apple.png")
        result.output.contains("intersection")
        result.task(":compareDirectories").outcome == SUCCESS
    }

    // helper methods
    private Path getBuildFile() {
        return too.getClassOutputDirectory().resolve("build.gradle")
    }

    private Path getSettingsFile() {
        return too.getClassOutputDirectory().resolve("settings.gradle")
    }
}

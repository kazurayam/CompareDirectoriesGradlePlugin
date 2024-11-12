package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Files
import java.nio.file.Path

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CompareDirectoriesPluginFunctionalTest extends Specification {

    private static TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(CompareDirectoriesPluginFunctionalTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(CompareDirectoriesPluginFunctionalTest.class).build()

    private static Path fixturesDir
    private Path outputFile
    private Path diffDir
    private Path nameStatusList

    // fixture methods
    def setupSpec() {
        too.cleanClassOutputDirectory()
        fixturesDir = too.getProjectDirectory().resolve("src/test/fixtures").toAbsolutePath()
        Path dataDir = too.resolveClassOutputDirectory().resolve("data")
        too.copyDir(fixturesDir, dataDir)
    }

    def setup() {
        settingsFile << ""
        buildFile << """
plugins {
    id("com.kazurayam.compare-directories") version "0.2.11"
}

compareDirectories {
    dirA = fileTree(layout.projectDirectory.dir("data/A")) { include "**/*" }
    dirB = fileTree(layout.projectDirectory.dir("data/B")) { include "**/*" }
    outputFile = layout.buildDirectory.file("out/differences.json")
    diffDir = layout.buildDirectory.dir("out/diff")
    charsetsToTry.add("Shift_JIS")
}

tasks.register("dircomp", com.kazurayam.dircomp.CompareDirectoriesTask) {
    dirA = fileTree(layout.projectDirectory.dir("src/test/fixtures/A")) { include "**/*" }
    dirB = fileTree(layout.projectDirectory.dir("src/test/fixtures/B")) { include "**/*" }
    outputFile = layout.buildDirectory.file("out/differences.json")
    nameStatusList = layout.buildDirectory.file("out/nameStatusList.tsv")
    diffDir = layout.buildDirectory.dir("out/diff")
    charsetsToTry.add("Shift_JIS")
    doFirst {
        delete layout.buildDirectory.dir("out")
    }
    doLast {
        println "output at " + layout.buildDirectory.dir("out").get()
    }
}
"""
        outputFile = too.resolveClassOutputDirectory().resolve( "build/out/differences.json").toAbsolutePath()
        diffDir = too.resolveClassOutputDirectory().resolve("build/out/diff").toAbsolutePath()
        nameStatusList = too.resolveClassOutputDirectory().resolve("build/out/nameStatusList.tsv").toAbsolutePath()

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
        Files.createDirectories(too.resolveClassOutputDirectory().resolve("build"))

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(too.resolveClassOutputDirectory().toFile())
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
        message.contains("apple.png")
        result.output.contains("intersection")
        result.task(":compareDirectories").outcome == SUCCESS
    }

    // helper methods
    private Path getBuildFile() {
        return too.resolveClassOutputDirectory().resolve("build.gradle")
    }

    private Path getSettingsFile() {
        return too.resolveClassOutputDirectory().resolve("settings.gradle")
    }
}

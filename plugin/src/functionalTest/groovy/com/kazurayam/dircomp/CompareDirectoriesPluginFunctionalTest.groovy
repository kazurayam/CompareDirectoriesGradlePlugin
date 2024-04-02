package com.kazurayam.dircomp

import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification
import spock.lang.TempDir
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CompareDirectoriesPluginFunctionalTest extends Specification {

    // fields
    @TempDir
    private File tempDir

    private static Path projectDir
    private static Path fixturesDir
    private Path outputFile
    private Path diffDir

    // fixture methods
    def setupSpec() {
        projectDir = Paths.get(".").toAbsolutePath().normalize()
        fixturesDir = projectDir.resolve("src/test/fixtures").toAbsolutePath()
    }

    def setup() {
        settingsFile << ""
        buildFile << """
plugins {
    id('com.kazurayam.compare-directories')
}

compareDirectories {
    dirA = fileTree("${fixturesDir}/A")
    dirB = fileTree("${fixturesDir}/B")
    outputFile = file("build/tmp/differences.json")
    diffDir = file("build/tmp/diff")
}

task dircomp(type: com.kazurayam.dircomp.CompareDirectoriesTask) {
    dirA = fileTree("${fixturesDir}/A") { exclude "**/*.png" }
    dirB = fileTree("${fixturesDir}/B") { exclude "**/*.png" }
    outputFile = file("build/tmp/differences.json")
    diffDir = file("build/tmp/diff")
    doFirst {
        println "dircomp.doFirst was executed"
    }
    doLast {
        println "dircomp.doLast was executed"
    }
}
"""
        outputFile = tempDir.toPath().resolve( "build/tmp/differences.json").toAbsolutePath()
        diffDir = tempDir.toPath().resolve("build/tmp/diff").toAbsolutePath()

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
        Files.createDirectories(tempDir.toPath().resolve("build"))

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(tempDir)
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

    def "can run dircomp task"() {
        given:
        assert Files.exists(fixturesDir)
        println "fixturesDir=${fixturesDir.toString()}"
        Files.createDirectories(tempDir.toPath().resolve("build"))

        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(tempDir)
                .withArguments("dircomp") // THIS IS THE DIFFERENCE
                .withPluginClasspath()
                .build()
        String message = outputFile.toFile().text
        println message

        then:
        message.contains("filesOnlyInA")
        message.contains("filesOnlyInB")
        message.contains("intersection")
        message.contains("modifiedFiles")
        ! message.contains("apple.png")    // the apple.png file is excluded
        result.output.contains("intersection")
        result.task(":dircomp").outcome == SUCCESS
    }

    // helper methods
    private Path getBuildFile() {
        return tempDir.toPath().resolve("build.gradle")
    }

    private Path getSettingsFile() {
        return tempDir.toPath().resolve("settings.gradle")
    }
}

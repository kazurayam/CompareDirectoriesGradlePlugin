package com.kazurayam.dircomp

import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner
import com.kazurayam.unittest.TestOutputOrganizer

import java.nio.file.Files
import java.nio.file.Path

class CompareDirectoriesPluginFunctionalTest extends Specification {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(CompareDirectoriesPluginFunctionalTest.class)
                    .subDirPath(CompareDirectoriesPluginFunctionalTest.class).build()

    // fields
    private static Path projectDir
    private static Path fixturesDir
    private static Path targetDir
    private Path outputFile
    private Path diffDir

    // fixture methods
    def setupSpec() {
        too.cleanClassOutputDirectory()
        projectDir = too.getProjectDir()
        fixturesDir = projectDir.resolve("src/test/fixtures").toAbsolutePath()
        targetDir = too.getClassOutputDirectory()
        too.copyDir(fixturesDir, targetDir)
    }

    def setup() {
        outputFile = targetDir.resolve( "build/tmp/differences.json").toAbsolutePath()
        diffDir = targetDir.resolve("build/tmp/diff").toAbsolutePath()
        settingsFile << ""
        buildFile << """
plugins {
    id('com.kazurayam.compare-directories')
}

compareDirectories {
    dirA = fileTree(dir: "${fixturesDir.toString()}/A")
    dirB = fileTree(dir: "${fixturesDir.toString()}/B")
    outputFile = file("build/tmp/differences.json")
    diffDir = file("build/tmp/diff")
}

task dircomp(type: com.kazurayam.dircomp.CompareDirectoriesTask) {
    dirA = fileTree(dir: "${fixturesDir.toString()}/A")
    dirB = fileTree(dir: "${fixturesDir.toString()}/B")
    outputFile = file("build/tmp/differences.json")
    diffDir = file("build/tmp/diff")
    doFirst {
        println "dircomp.doFirst was executed"
    }
    doLast {
        println "dircomp2.doLast was executed"
    }
}
"""
    }
    //def cleanup() {}
    //def cleanupSpec() {}

    // feature methods
    def "can run compareDirectories task"() {
        given:
        assert Files.exists(fixturesDir)
        println "fixturesDir=${fixturesDir.toString()}"
        Files.createDirectories(targetDir.toPath().resolve("build"))

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("compareDirectories")
        runner.withProjectDir(targetDir)
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

    def "can run dircomp task"() {
        given:
        assert Files.exists(fixturesDir)
        println "fixturesDir=${fixturesDir.toString()}"
        Files.createDirectories(targetDir.toPath().resolve("build"))

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("dircomp") // THIS IS THE DIFFERENCE
        runner.withProjectDir(targetDir)
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
        return targetDir.toPath().resolve("build.gradle")
    }

    private Path getSettingsFile() {
        return targetDir.toPath().resolve("settings.gradle")
    }
}

package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Files
import java.nio.file.Path

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

/**
 * check if the CompareDirectoriesPlugin + CompareDirectoriesTask handles various
 * errors appropriately. For example, if dirA parameter is NOT given when you run
 * the task, what happends?
 */
class CompareDirectoriesPluginErrorHandlingTest extends Specification {

    private static TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(CompareDirectoriesPluginErrorHandlingTest.class)
                    .subDirPath(CompareDirectoriesPluginErrorHandlingTest.class).build()

    private static Path fixturesDir
    private static Path settingsFile
    private static Path buildFile
    private Path outputFile
    private Path diffDir

    def setupSpec() {
        fixturesDir = too.getProjectDir().resolve("src/test/fixtures").toAbsolutePath()
    }

    def setup() {
        too.cleanClassOutputDirectory()
        settingsFile = too.getClassOutputDirectory().resolve("settings.gradle");
        buildFile = too.getClassOutputDirectory().resolve("build.gradle")
        settingsFile << ""
        buildFile << """
plugins {
    id('com.kazurayam.compare-directories')
}

compareDirectories {}
"""
    }

    def "when no values assigned"() {
        given:
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
        result.task(":compareDirectories").outcome == SUCCESS
    }
}
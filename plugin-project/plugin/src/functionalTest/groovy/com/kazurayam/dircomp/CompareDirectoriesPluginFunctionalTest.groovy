package com.kazurayam.dircomp

import spock.lang.Specification
import spock.lang.TempDir
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Path
import java.nio.file.Paths

class CompareDirectoriesPluginFunctionalTest extends Specification {
    @TempDir
    private File tempDir

    private File getBuildFile() {
        return new File(tempDir, "build.gradle")
    }

    private File getSettingsFile() {
        return new File(tempDir, "settings.gradle")
    }

    private Path projectDir = Paths.get(".").toAbsolutePath().normalize()
    private Path fixturesDir = projectDir.resolve("src/test/fixtures")
    private Path outputFile = projectDir.resolve( "build/differences.json")
    private Path diffDir = projectDir.resolve("build/diff")

    def "can run task"() {
        given:
        println "projectDir=" + projectDir.toString()

        settingsFile << ""
        buildFile << """
plugins {
    id('com.kazurayam.compareDirectories')
}
compareDirectories {
    dirA = "${fixturesDir.toString()}/A"
    dirB = "${fixturesDir.toString()}/B"
    outputFile = "${outputFile.toString()}"
    diffDir = "${diffDir.toString()}"
}
"""

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("compareDirectories")
        runner.withProjectDir(tempDir)
        def result = runner.build()
        String message = outputFile.toFile().text
        println message


        then:
        message.contains("filesOnlyInA")
        message.contains("filesOnlyInB")
        message.contains("intersection")
        message.contains("modifiedFiles")
    }
}

package com.kazurayam.dircomp

import groovy.json.JsonOutput
import org.gradle.internal.impldep.org.junit.BeforeClass
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertTrue

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Reproduce the issue#25
 * https://github.com/kazurayam/CompareDirectoriesGradlePlugin/issues/25
 *
 */
class Issue25Test {

    private Path baseDir
    private Path dirA
    private Path dirB
    private Path outputFile
    private Path diffDir

    @BeforeClass
    static void beforeClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","debug")
    }

    @BeforeEach
    void setup() {
        // setup properties
        baseDir = Paths.get("../../../NineBreakHomePage")
        assert Files.exists(baseDir)
        dirA = baseDir.resolve("site/build/tmp/snapshot")
        dirB = baseDir.resolve("site/src")
        assert Files.exists(dirA)
        assert Files.exists(dirB)
        Path dir = Paths.get("build/tmp/Issue25Test")
        outputFile = dir.resolve("differences.json")
        diffDir = dir.resolve("diffout")
        Files.createDirectories(diffDir)
    }

    @Test
    void test_smoke() {

        CompareDirectoriesAction actionObject =
                new CompareDirectoriesAction(baseDir, dirA, dirB, outputFile, diffDir)

        int numb = actionObject.action()

        assertTrue(numb > 0, "numb is 0")
    }
}

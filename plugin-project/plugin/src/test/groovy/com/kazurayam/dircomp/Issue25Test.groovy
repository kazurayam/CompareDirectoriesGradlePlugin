package com.kazurayam.dircomp

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
    private Path diffDir

    @BeforeEach
    void setup() {
        // setup properties
        baseDir = Paths.get("../../../NineBreakHomePage")
        assert Files.exists(baseDir)
        dirA = baseDir.resolve("site/build/tmp/snapshot")
        dirB = baseDir.resolve("site/src")
        assert Files.exists(dirA)
        assert Files.exists(dirB)
        diffDir = Paths.get(".").resolve("build/tmp/diffout")
        Files.createDirectories(diffDir)
    }

    @Test
    void test_smoke() {
        CompareDirectories comparator =
                new CompareDirectories(baseDir, dirA, dirB)
        DirectoriesDifferences differences =
                comparator.getDifferences()
        int numb = differences.makeDiffFiles(diffDir)
        assertTrue(numb > 0, "numb is 0")
    }
}

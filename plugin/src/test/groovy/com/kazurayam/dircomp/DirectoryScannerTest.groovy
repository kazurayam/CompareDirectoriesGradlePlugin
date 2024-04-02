package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class DirectoryScannerTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(DirectoryScannerTest.class)
                    .subDirPath(DirectoryScannerTest.class).build()

    @BeforeAll
    static void beforeAll() {
        too.cleanClassOutputDirectory()
        Path fixturesDir = too.projectDir.resolve("src/test/fixtures")
        Path targetDir = too.getClassOutputDirectory()
        too.copyDir(fixturesDir, targetDir)
    }

    @Test
    void test_getFiles() {
        List<Path> files =
                new DirectoryScanner(too.getClassOutputDirectory())
                        .scan()
                        .getFiles()
        assertThat(files).hasSizeGreaterThan(0)
        files.each { println it }
    }

    @Test
    void test_getSubPaths() {
        Set<String> subPaths =
                new DirectoryScanner(too.getClassOutputDirectory())
                        .scan()
                        .getSubPaths()
        assertThat(subPaths).hasSizeGreaterThan(0)
        subPaths.stream().sorted().each { println it}
    }
}

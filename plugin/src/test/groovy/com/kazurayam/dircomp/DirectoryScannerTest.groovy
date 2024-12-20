package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertEquals

class DirectoryScannerTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(DirectoryScannerTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(DirectoryScannerTest.class).build()

    static Path fixturesDir

    @BeforeAll
    static void beforeAll() {
        fixturesDir = too.getProjectDirectory().resolve("src/test/fixtures")
        too.cleanClassOutputDirectory()
        Path targetDir = too.resolveClassOutputDirectory()
        too.copyDir(fixturesDir, targetDir)
    }

    @Test
    void test_getFiles() {
        Set<Path> files =
                new DirectoryScanner(too.resolveClassOutputDirectory())
                        .scan()
                        .getFiles()
        assertThat(files).hasSizeGreaterThan(0)
        files.each { println it }
    }

    @Test
    void test_getSubPaths() {
        Set<String> subPaths =
                new DirectoryScanner(too.resolveClassOutputDirectory())
                        .scan()
                        .getSubPaths()
        assertThat(subPaths).hasSizeGreaterThan(0)
        subPaths.stream().sorted().each { println it}
    }

    @Test
    void testSmoke() {
        Path dirA = fixturesDir.resolve("A")
        Set<String> subPaths = new DirectoryScanner(dirA).scan().getSubPaths()
        for (String p : subPaths) {
            println p
        }
        assertEquals(8, subPaths.size())
    }
}

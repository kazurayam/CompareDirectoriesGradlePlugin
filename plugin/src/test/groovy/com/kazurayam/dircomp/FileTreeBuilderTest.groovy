package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class FileTreeBuilderTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(FileTreeBuilderTest.class)
                    .subDirPath(FileTreeBuilderTest.class).build()

    @BeforeAll
    static void beforeAll() {
        too.cleanClassOutputDirectory()
        Path fixturesDir = too.projectDir.resolve("src/test/fixtures")
        Path targetDir = too.getClassOutputDirectory()
        too.copyDir(fixturesDir, targetDir)
    }

    @Test
    void test_getContainedFiles() {
        List<Path> containedFiles =
                FileTreeBuilder.containedFiles(too.getClassOutputDirectory())
        assertThat(containedFiles).hasSizeGreaterThan(0)
        //containedFiles.each { println it }
    }

    @Test
    void test_scan() {
        Set<String> subPaths = FileTreeBuilder.scan(too.getClassOutputDirectory())
        assertThat(subPaths).hasSizeGreaterThan(0)
        subPaths.stream().sorted().each { println it}
    }
}

package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class FileCollectionsComparatorTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(FileCollectionsComparatorTest.class)
                    .subDirPath(FileCollectionsComparatorTest.class).build()

    @BeforeAll
    static void beforeAll() {
        too.cleanClassOutputDirectory()
        Path fixturesDir = too.projectDir.resolve("src/test/fixtures")
        Path targetDir = too.getClassOutputDirectory()
        too.copyDir(fixturesDir, targetDir)
    }

    @Test
    void test_toSubPaths() {
        Path dir = too.getClassOutputDirectory().resolve("A")
        List<Path> containedFiles = FileTreeBuilder.containedFiles(dir)
        Set<String> subPaths = FileCollectionsComparator.toSubPaths(dir, containedFiles)
        assertThat(subPaths).hasSizeGreaterThan(0)
        subPaths.stream().sorted().each { println it}
    }
}

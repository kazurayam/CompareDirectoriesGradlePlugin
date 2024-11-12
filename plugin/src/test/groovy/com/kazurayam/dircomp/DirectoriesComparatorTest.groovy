package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class DirectoriesComparatorTest {

    private static final Logger logger = LoggerFactory.getLogger(DirectoriesComparatorTest)

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(DirectoriesComparatorTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(DirectoriesComparatorTest.class).build()

    @BeforeAll
    static void beforeAll() {
        too.cleanClassOutputDirectory()
        Path fixturesDir = too.getProjectDirectory().resolve("src/test/fixtures")
        Path targetDir = too.cleanClassOutputDirectory()
        too.copyDir(fixturesDir, targetDir)
    }

    @Test
    void test_constructor_FileTree() {
        Path dirA = too.resolveClassOutputDirectory().resolve("A")
        Set<Path> filesA = new DirectoryScanner(dirA).scan().getFiles()
        Path dirB = too.resolveClassOutputDirectory().resolve("B")
        Set<Path> filesB = new DirectoryScanner(dirB).scan().getFiles()
        DirectoriesComparator comparator =
                new DirectoriesComparator(dirA, filesA, dirB, filesB)
        comparator.doCompare()
        DirectoriesDifferences differences = comparator.getDifferences()
        println differences
        assertThat(differences.filesOnlyInA.size()).isGreaterThan(0)
        assertThat(differences.filesOnlyInB.size()).isGreaterThan(0)
        assertThat(differences.intersection.size()).isGreaterThan(0)
        assertThat(differences.modifiedFiles.size()).isGreaterThan(0)
    }

    @Test
    void test_toSubPaths() {
        Path dir = too.resolveClassOutputDirectory().resolve("A")
        logger.info("dir : " + dir.toString())
        Set<Path> files = new DirectoryScanner(dir).scan().getFiles()
        Set<String> subPaths = DirectoriesComparator.toSubPaths(dir, files)
        assertThat(subPaths).hasSizeGreaterThan(0)
        subPaths.stream().sorted().each { println it}
    }
}

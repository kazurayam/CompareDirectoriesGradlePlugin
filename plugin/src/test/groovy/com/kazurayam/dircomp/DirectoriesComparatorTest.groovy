package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.gradle.api.file.ConfigurableFileTree
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class DirectoriesComparatorTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(DirectoriesComparatorTest.class)
                    .subDirPath(DirectoriesComparatorTest.class).build()

    @BeforeAll
    static void beforeAll() {
        too.cleanClassOutputDirectory()
        Path fixturesDir = too.projectDir.resolve("src/test/fixtures")
        Path targetDir = too.getClassOutputDirectory()
        too.copyDir(fixturesDir, targetDir)
    }

    @Test
    void test_constructor_FileTree() {
        Path dirA = too.getClassOutputDirectory().resolve("A")
        Set<Path> filesA = new DirectoryScanner(dirA).scan().getFiles()
        Path dirB = too.getClassOutputDirectory().resolve("B")
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
        Path dir = too.getClassOutputDirectory().resolve("A")
        Set<Path> files = new DirectoryScanner(dir).scan().getFiles()
        Set<String> subPaths = DirectoriesComparator.toSubPaths(dir, files)
        assertThat(subPaths).hasSizeGreaterThan(0)
        subPaths.stream().sorted().each { println it}
    }
}

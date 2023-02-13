package com.kazurayam.dircomp

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class DirectoriesComparatorTest {

    private static Path projectDir
    private static Path sourceDir
    private static Path targetDir
    private DirectoriesComparator instance

    private static final String SOURCE_DIR_RELATIVE_PATH = "src/test/fixtures/A"
    private static final String TARGET_DIR_RELATIVE_PATH = "src/test/fixtures/B"

    @BeforeAll
    static void beforeAll() {
        projectDir = Paths.get(".")
        sourceDir = projectDir.resolve(SOURCE_DIR_RELATIVE_PATH)
        targetDir = projectDir.resolve(TARGET_DIR_RELATIVE_PATH)
    }

    @BeforeEach
    void beforeEach() {
        instance =
                new DirectoriesComparator(sourceDir, targetDir)
    }

    @Test
    void test_getSourceDir() {
        assertEquals(sourceDir.toAbsolutePath().normalize(),
                instance.getSourceDir().toAbsolutePath().normalize())
    }

    @Test
    void test_getTargetDir() {
        assertEquals(targetDir.toAbsolutePath().normalize(),
                instance.getTargetDir().toAbsolutePath().normalize())
    }

    @Test
    void test_getFilesOnlyInA() {
        DirectoriesDifferences differences = instance.getDifferences()
        Set<Path> files = differences.getFilesOnlyInA()
        assertEquals(1, files.size())
        assertTrue(files.contains(Paths.get("sub/i.txt")))
    }

    @Test
    void test_getFilesOnlyInB() {
        DirectoriesDifferences differences = instance.getDifferences()
        Set<Path> files = differences.getFilesOnlyInB()
        assertEquals(2, files.size())
        assertTrue(files.contains(Paths.get("j.txt")))
        assertTrue(files.contains(Paths.get("sub/h.txt")))
    }

    @Test
    void test_getIntersection() {
        DirectoriesDifferences differences = instance.getDifferences()
        Set<Path> files = differences.getIntersection()
        assertEquals(4, files.size())
        assertTrue(files.contains(Paths.get("d.txt")))
        assertTrue(files.contains(Paths.get("e.txt")))
        assertTrue(files.contains(Paths.get("sub/f.txt")))
        assertTrue(files.contains(Paths.get("sub/g.txt")))
    }

    @Test
    void test_getModifiedFiles() {
        DirectoriesDifferences differences = instance.getDifferences()
        Set<Path> files = differences.getModifiedFiles()
        assertEquals(1, files.size())
        assertTrue(files.contains(Paths.get("sub/g.txt")))
    }

}

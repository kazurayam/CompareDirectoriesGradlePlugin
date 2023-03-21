package com.kazurayam.dircomp

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class CompareDirectoriesTest {

    private static Path projectDir
    private static Path sourceDir
    private static Path targetDir
    private CompareDirectories instance

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
                new CompareDirectories(sourceDir, targetDir)
    }

    @Test
    void test_getDirA() {
        assertEquals(sourceDir.toAbsolutePath().normalize(),
                instance.getDirA().toAbsolutePath().normalize())
    }

    @Test
    void test_getDirB() {
        assertEquals(targetDir.toAbsolutePath().normalize(),
                instance.getDirB().toAbsolutePath().normalize())
    }

    @Test
    void test_getDifferences() {
        DirectoriesDifferences differences = instance.getDifferences()
        assertNotNull(differences)
    }

}

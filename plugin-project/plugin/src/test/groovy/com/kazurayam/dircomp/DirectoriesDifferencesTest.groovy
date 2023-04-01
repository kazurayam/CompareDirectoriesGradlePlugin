package com.kazurayam.dircomp

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DirectoriesDifferencesTest {

    private static Path projectDir
    private static Path dirA
    private static Path dirB
    private static Path diffDir
    private CompareDirectories instance
    private DirectoriesDifferences differences

    private static final String DIR_A_RELATIVE_PATH = "src/test/fixtures/A"
    private static final String DIR_B_RELATIVE_PATH = "src/test/fixtures/B"
    private static final String DIFFDIR_RELATIVE_PATH = "build/tmp/diff"

    @BeforeAll
    static void beforeAll() {
        projectDir = Paths.get(".")
        dirA = projectDir.resolve(DIR_A_RELATIVE_PATH)
        dirB = projectDir.resolve(DIR_B_RELATIVE_PATH)
        diffDir = projectDir.resolve(DIFFDIR_RELATIVE_PATH)
        Files.createDirectories(diffDir)
    }

    @BeforeEach
    void beforeEach() {
        instance =
                new CompareDirectories(dirA, dirB)
        differences =
                instance.getDifferences()
    }

    @Test
    void testSerialize() {
        String jsonResult = differences.serialize()
        assertNotNull(jsonResult)
        println jsonResult
    }

    @Test
    void testDeserialize() {
        Path tmpFile = Files.createTempFile("DirectoriesDifferencesTest", "tmp.json")
        tmpFile.text = differences.serialize()
        assertTrue(Files.size(tmpFile) > 0)
        //
        DirectoriesDifferences instance = DirectoriesDifferences.deserialize(tmpFile)
        assertNotNull(instance)
        println instance.serialize()
    }

    @Test
    void testMakeDiffFiles() {
        int result = differences.makeDiffFiles(diffDir)
        assertEquals(1, result)
    }
}

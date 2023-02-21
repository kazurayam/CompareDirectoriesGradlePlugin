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
    private static Path sourceDir
    private static Path targetDir
    private DirectoriesComparator instance
    private DirectoriesDifferences differences

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
}

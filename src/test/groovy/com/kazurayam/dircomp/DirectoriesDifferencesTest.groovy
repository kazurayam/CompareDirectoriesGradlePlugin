package com.kazurayam.dircomp

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

class DirectoriesDifferencesSerializerTest {

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
    void testWriteWithDefaultPrettyPrinter() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult =
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(differences);
        println jsonResult
    }
}

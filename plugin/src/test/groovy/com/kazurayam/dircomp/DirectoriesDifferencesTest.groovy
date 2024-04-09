package com.kazurayam.dircomp

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path

import com.kazurayam.unittest.TestOutputOrganizer
import static org.junit.jupiter.api.Assertions.*

class DirectoriesDifferencesTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(DirectoriesDifferencesTest.class)
                    .subDirPath(DirectoriesDifferencesTest.class).build()

    private static Path dirA
    private static Path dirB
    private static Path diffDir
    private static Path workDir
    private DirectoriesComparator instance
    private DirectoriesDifferences differences

    private static final String DIR_A_RELATIVE_PATH = "src/test/fixtures/A"
    private static final String DIR_B_RELATIVE_PATH = "src/test/fixtures/B"
    private static final String DIFFDIR_RELATIVE_PATH = "build/tmp/test/diff"

    @BeforeAll
    static void beforeAll() {
        Path projectDir = too.getProjectDir()
        dirA = projectDir.resolve(DIR_A_RELATIVE_PATH)
        dirB = projectDir.resolve(DIR_B_RELATIVE_PATH)
        diffDir = projectDir.resolve(DIFFDIR_RELATIVE_PATH)
        Files.createDirectories(diffDir)
        workDir = projectDir.resolve("build/tmp/test")
        Files.createDirectories(workDir)
    }

    @BeforeEach
    void beforeEach() {
        Set<Path> contentA = new DirectoryScanner(dirA).scan().getFiles()
        Set<Path> contentB = new DirectoryScanner(dirB).scan().getFiles()
        instance = new DirectoriesComparator(dirA, contentA, dirB, contentB)
        differences = instance.getDifferences()
    }

    @Test
    void testSerialize() {
        String jsonResult = differences.serialize()
        assertNotNull(jsonResult)
        println jsonResult
        assertTrue(jsonResult.contains("取扱説明書"))
    }

    @Test
    void testDeserialize() {
        Path tmpFile = workDir.resolve("tmp.json")
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
        assertEquals(2, result)
    }

    @Test
    void testReadAllLines_PNG() {
        Path apple = dirA.resolve("apple.png")
        List<String> content = DirectoriesDifferences.readAllLines(apple)
        assertTrue(content.get(0).contains("Failed to read"))
        assertTrue(content.get(0).contains("apple.png"))
        assertTrue(content.get(0).contains("as a text in UTF-8"))
    }
}

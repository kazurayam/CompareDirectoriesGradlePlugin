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
    private static Path workDir
    private static Path diffDir
    private static Path fixturesDir

    private DirectoriesDifferences differences

    @BeforeAll
    static void beforeAll() {
        too.cleanClassOutputDirectory()
        fixturesDir = too.getProjectDir().resolve("src/test/fixtures")
        dirA = fixturesDir.resolve("A")
        dirB = fixturesDir.resolve("B")
        workDir = too.getClassOutputDirectory()
        diffDir = workDir.resolve("diff")
        Files.createDirectories(diffDir)
    }

    @BeforeEach
    void beforeEach() {
        Set<Path> contentA = new DirectoryScanner(dirA).scan().getFiles()
        Set<Path> contentB = new DirectoryScanner(dirB).scan().getFiles()
        DirectoriesComparator dirComp = new DirectoriesComparator(dirA, contentA, dirB, contentB)
        differences = dirComp.getDifferences()
    }

    @Test
    void testToJSON() {
        String jsonResult = differences.toJSON()
        assertNotNull(jsonResult)
        println jsonResult
        assertTrue(jsonResult.contains("取扱説明書"))
    }

    @Test
    void testSerializeAndDeserialize() {
        Path differencesFile = workDir.resolve("differences.json")
        differences.serialize(differencesFile)
        assertTrue(Files.size(differencesFile) > 0)
        //
        DirectoriesDifferences instance = DirectoriesDifferences.deserialize(differencesFile)
        assertNotNull(instance)
        println instance.toJSON()
    }

    @Test
    void testMakeDiffFiles() {
        int result = differences.makeDiffFiles(diffDir)
        assertEquals(3, result)
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

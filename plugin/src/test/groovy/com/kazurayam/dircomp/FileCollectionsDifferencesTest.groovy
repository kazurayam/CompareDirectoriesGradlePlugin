package com.kazurayam.dircomp

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.unittest.TestOutputOrganizer
import static org.junit.jupiter.api.Assertions.*

class FileCollectionsDifferencesTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(FileCollectionsDifferencesTest.class)
                    .subDirPath(FileCollectionsDifferencesTest.class).build()

    private static Path dirA
    private static Path dirB
    private static Path diffDir
    private static Path workDir
    private FileCollectionsComparator instance
    private FileCollectionsDifferences differences

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
        List<Path> contentA = FileTreeBuilder.containedFiles(dirA)
        List<Path> contentB = FileTreeBuilder.containedFiles(dirB)
        instance = new FileCollectionsComparator(dirA, contentA, dirB, contentB)
        differences = instance.getDifferences()
    }

    @Test
    void testSerialize() {
        String jsonResult = differences.serialize()
        assertNotNull(jsonResult)
        println jsonResult
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
        assertEquals(1, result)
    }
}

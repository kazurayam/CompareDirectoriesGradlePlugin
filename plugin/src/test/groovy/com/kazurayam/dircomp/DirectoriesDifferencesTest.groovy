package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.gradle.internal.impldep.org.junit.Ignore
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.*

class DirectoriesDifferencesTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(DirectoriesDifferencesTest.class)
                    .subDirPath(DirectoriesDifferencesTest.class).build()

    private static Path fixturesDir
    private static Path dirA
    private static Path dirB
    private static DirectoriesDifferences differences

    @BeforeAll
    static void beforeAll() {
        fixturesDir = too.getProjectDir().resolve("src/test/fixtures")
        dirA = fixturesDir.resolve("A")
        dirB = fixturesDir.resolve("B")
        Set<Path> contentA = new DirectoryScanner(dirA).scan().getFiles()
        Set<Path> contentB = new DirectoryScanner(dirB).scan().getFiles()
        DirectoriesComparator dirComp = new DirectoriesComparator(dirA, contentA, dirB, contentB)
        differences = dirComp.getDifferences()
        differences.addCharsetsToTry(Arrays.asList("Shift_JIS"))
        //
        too.cleanClassOutputDirectory()
    }

    @BeforeEach
    void beforeEach() {}

    @Test
    void testToJSON() {
        String jsonResult = differences.toJSON()
        assertNotNull(jsonResult)
        println jsonResult
        assertTrue(jsonResult.contains("取扱説明書"))
        assertFalse(jsonResult.contains("file:///"))
    }

    @Test
    void testSerializeAndDeserialize() {
        Path workDir = too.getMethodOutputDirectory("testSerializeAndDeserialize")
        //
        Path differencesFile = workDir.resolve("differences.json")
        differences.serialize(differencesFile)
        assertTrue(Files.size(differencesFile) > 0)
        //
        DirectoriesDifferences instance = DirectoriesDifferences.deserialize(differencesFile)
        assertNotNull(instance)
        println instance.toJSON()
    }

    @Ignore
    @Test
    void testMakeDiffFiles() {
        //int result = differences.makeDiffFiles(diffDir)
        //assertEquals(3, result)
    }

    @Test
    void testReadAllLines_PNG() {
        Path apple = dirA.resolve("apple.png")
        List<String> content = differences.readAllLines(apple)
        assertTrue(content.get(0).contains("Failed to read"))
        assertTrue(content.get(0).contains("apple.png"))
    }

    @Test
    void testReadAllLines_Shift_JIS() {
        Path apple = dirA.resolve("このファイルはシフトJISだよん.txt")
        List<String> content = differences.readAllLines(apple)
        assertTrue(content.get(0).contains("シフトJIS"))
    }

    @Test
    void test_compileNameStatus_filesOnlyInA() {
        String line = differences.compileNameStatus("sub/i.txt", dirA, dirB)
        assertEquals("sub/i.txt\tD\t2024-03-29T09:45:03.675413\t-\t-\t1\t-\t-", line)
    }

    @Test
    void test_compileNameStatus_filesOnlyInB() {
        String line = differences.compileNameStatus("j.txt", dirA, dirB)
        assertEquals("j.txt\tA\t-\t-\t2024-03-29T09:45:03.676875\t-\t-\t1", line)
    }

    @Test
    void test_compileNameStatus_sameSize() {
        String line = differences.compileNameStatus("apple.png", dirA, dirB)
        assertEquals("apple.png\t-\t2024-03-29T09:45:03.672855\t<\t2024-03-29T09:45:03.675864\t3655\t=\t3655", line)
    }

    @Test
    void test_compileNameStatus_modified() {
        String line = differences.compileNameStatus("sub/g.txt", dirA, dirB)
        assertEquals("sub/g.txt\tM\t2024-03-29T09:45:03.675207\t<\t2024-03-29T09:45:03.677459\t1\t<\t12", line)
    }

    @Test
    void test_reportNameStatusList() {
        Path workDir = too.getMethodOutputDirectory("test_reportNameStatusList")
        Path outputText = workDir.resolve("nameStatusList.tsv")
        differences.reportNameStatusList(outputText)
        assertTrue(Files.exists(outputText))
    }
}

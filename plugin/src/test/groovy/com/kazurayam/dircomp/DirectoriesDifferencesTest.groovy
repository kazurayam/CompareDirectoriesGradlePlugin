package com.kazurayam.dircomp

import com.kazurayam.unittest.TestOutputOrganizer
import org.gradle.internal.impldep.org.junit.Ignore
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DirectoriesDifferencesTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(DirectoriesDifferencesTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(DirectoriesDifferencesTest.class).build()

    private static Path fixturesDir
    private static Path dirA
    private static Path dirB
    private static DirectoriesDifferences differences

    @BeforeAll
    static void beforeAll() {
        too.cleanClassOutputDirectory()
        fixturesDir = too.getProjectDirectory().resolve("src/test/fixtures")
        dirA = fixturesDir.resolve("A")
        dirB = fixturesDir.resolve("B")
        Set<Path> contentA = new DirectoryScanner(dirA).scan().getFiles()
        Set<Path> contentB = new DirectoryScanner(dirB).scan().getFiles()
        DirectoriesComparator dirComp = new DirectoriesComparator(dirA, contentA, dirB, contentB)
        differences = dirComp.getDifferences()
        differences.addCharsetsToTry(Arrays.asList("Shift_JIS"))
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
        Path workDir = too.cleanMethodOutputDirectory("testSerializeAndDeserialize")
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
        assertEquals("\tsub/i.txt\tD\t1\t-\t-\t2024-03-29T09:45:03\t-\t-", line)
    }

    @Test
    void test_compileNameStatus_filesOnlyInB() {
        String line = differences.compileNameStatus("j.txt", dirA, dirB)
        assertEquals("\tj.txt\tA\t-\t-\t1\t-\t-\t2024-03-29T09:45:03", line)
    }

    @Test
    void test_compileNameStatus_sameSize() {
        String line = differences.compileNameStatus("apple.png", dirA, dirB)
        assertEquals("\tapple.png\tM\t3655\t<\t416396\t2024-03-29T09:45:03\t<\t2024-11-12T08:51:35", line)
    }

    @Test
    void test_compileNameStatus_modified() {
        String line = differences.compileNameStatus("sub/g.txt", dirA, dirB)
        assertEquals("\tsub/g.txt\tM\t1\t<\t12\t2024-03-29T09:45:03\t<\t2024-03-29T09:45:03", line)
    }

    @Test
    void test_reportNameStatusList() {
        Path workDir = too.resolveMethodOutputDirectory("test_reportNameStatusList")
        Path outputText = workDir.resolve("nameStatusList.tsv")
        differences.reportNameStatusList(outputText)
        assertTrue(Files.exists(outputText))
    }

    @Test
    void test_ancestorDirectoryOf() {
        Path dirA = Paths.get("/Users/foo/bar/buz")
        Path dirB = Paths.get("/Users/foo/poo/woo")
        Path ancestor = DirectoriesDifferences.ancestorDirectoryOf(dirA, dirB)
        assertEquals(Paths.get("/Users/foo"), ancestor)
    }
}

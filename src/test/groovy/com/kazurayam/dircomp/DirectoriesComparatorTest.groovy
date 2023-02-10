package com.kazurayam.dircomp

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import static org.junit.jupiter.api.Assertions.*

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

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
        RelativeFilePathCollector sourceCollector = new RelativeFilePathCollector(sourceDir)
        Files.walkFileTree(sourceDir, sourceCollector)
        RelativeFilePathCollector targetCollector = new RelativeFilePathCollector(targetDir)
        Files.walkFileTree(targetDir, targetCollector)
        instance =
                new DirectoriesComparator(projectDir,
                        sourceDir, sourceCollector.get(),
                        targetDir, targetCollector.get())
    }

    @Test
    void test_getProjectDir() {
        assertEquals(projectDir.toAbsolutePath().normalize(),
                instance.getProjectDir().toAbsolutePath().normalize())
    }

    @Test
    void test_getSourceDir() {
        assertEquals(sourceDir.toAbsolutePath().normalize(),
                instance.getSourceDir().toAbsolutePath().normalize())
    }

    @Test
    void test_getSourceDirRelativeToProjectDir() {
        assertEquals(SOURCE_DIR_RELATIVE_PATH,
                instance.getSourceDirRelativeToProjectDir().toString())
    }

    @Test
    void test_getTargetDir() {
        assertEquals(targetDir.toAbsolutePath().normalize(),
                instance.getTargetDir().toAbsolutePath().normalize())
    }

    @Test
    void test_getTargetDirRelativeToProjectDir() {
        assertEquals(TARGET_DIR_RELATIVE_PATH,
                instance.getTargetDirRelativeToProjectDir().toString())
    }

    @Test
    void test_getSourceRemainder() {
        List<Path> sourceRemainder = instance.getSourceRemainder()
        assertEquals(2, sourceRemainder.size())
        assertTrue(sourceRemainder.contains(Paths.get("sub/f.txt")))
        assertTrue(sourceRemainder.contains(Paths.get("sub/i.txt")))
    }

    @Test
    void test_getTargetRemainder() {
        List<Path> targetRemainder = instance.getTargetRemainder()
        assertEquals(2, targetRemainder.size())
        assertTrue(targetRemainder.contains(Paths.get("j.txt")))
        assertTrue(targetRemainder.contains(Paths.get("sub/h.txt")))
    }

    @Test
    void test_getIntersection() {
        List<Path> intersection = instance.getIntersection()
        assertEquals(3, intersection.size())
        assertTrue(intersection.contains(Paths.get("d.txt")))
        assertTrue(intersection.contains(Paths.get("e.txt")))
        assertTrue(intersection.contains(Paths.get("sub/g.txt")))
    }

}

class RelativeFilePathCollector extends SimpleFileVisitor<Path> {
    private Path baseDir
    private Set<Path> filePaths = new HashSet<Path>()
    RelativeFilePathCollector(Path baseDir) {
        this.baseDir = baseDir
    }
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        Path relativePath = baseDir.relativize(file)
        filePaths.add(relativePath)
        return FileVisitResult.CONTINUE
    }
    public Set<Path> get() {
        return filePaths
    }

}
package com.kazurayam.dircomp

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import java.nio.file.Path
import java.nio.file.Paths

class FileTreeBuilderTest {

    private Path fixtures = Paths.get("./src/test/fixtures")

    @Test
    void testSmoke() {
        Path dirA = fixtures.resolve("A")
        Set<Path> subPaths = FileTreeBuilder.scan(dirA)
        for (Path p : subPaths) {
            println p
        }
        assertEquals(5, subPaths.size())
    }
}

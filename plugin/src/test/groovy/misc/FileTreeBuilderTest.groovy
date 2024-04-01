package misc

import com.kazurayam.dircomp.FileTreeBuilder
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import java.nio.file.Path
import java.nio.file.Paths

class FileTreeBuilderTest {

    private Path fixtures = Paths.get("./src/test/fixtures")

    @Test
    void testSmoke() {
        Path dirA = fixtures.resolve("A")
        Set<String> subPaths = FileTreeBuilder.scan(dirA)
        for (String p : subPaths) {
            println p
        }
        assertEquals(6, subPaths.size())
    }
}

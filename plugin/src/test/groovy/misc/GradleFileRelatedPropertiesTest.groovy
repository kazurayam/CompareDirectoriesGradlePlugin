package misc

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

/**
 * Learn the Gradle API for files:
 * - RegularFileProperty https://docs.gradle.org/current/javadoc/org/gradle/api/file/RegularFileProperty.html
 * - DirectoryProperty https://docs.gradle.org/current/javadoc/org/gradle/api/file/DirectoryProperty.html
 *
 * I will try to find out how those Gradle File API works with
 * - java.io.File API and
 * - java.nio.file.Path and other Java 8 APIs
 */
class GradleFileRelatedPropertiesTest {

    private Project project

    @BeforeEach
    void beforeEach() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    void test_RegularFile() {
        project.getObjects()
        //throw new RuntimeException("TODO")
    }
}

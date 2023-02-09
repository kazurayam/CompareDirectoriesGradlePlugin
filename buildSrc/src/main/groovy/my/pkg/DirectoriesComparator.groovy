package my.pkg

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.FileCollection
import java.nio.file.Paths

abstract public class DirectoriesComparator extends DefaultTask {

    @Input
    abstract public Property<String> getSourceDir()

    @Input
    abstract public Property<String> getTargetDir()

    @Input
    abstract public Property<FileTree> getSourceFileTree()

    @Input
    abstract public Property<FileTree> getTargetFileTree()

    public DirectoriesComparator() {
        getSourceDir().convention(".")
        getTargetDir().convention(".")
    }

    @TaskAction
    public void action() {
        println("sourceDir : ${getSourceDir().get()}")
        println("targetDir : ${getTargetDir().get()}")
        //
        println("sourceFileTree: ${getSourceFileTree().get().toString()}")
        FileTree sourceTree = getSourceFileTree().get()
        sourceTree.each { File file ->
            println("    " + file)
        }
        //
        println("targetFileTree: ${getTargetFileTree().get().toString()}")
        FileTree targetTree = getTargetFileTree().get()
        targetTree.each {File file ->
            println("    " + file)
        }
    }
}

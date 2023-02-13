package com.kazurayam.dircomp
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Paths
import java.nio.file.Path

abstract class DirectoriesComparatorTask extends DefaultTask {

    @Input
    abstract Property<String> getSourceDir()

    @Input
    abstract Property<String> getTargetDir()

    private ProjectLayout
    DirectoriesComparatorTask() {
        getSourceDir().convention(".")
        getTargetDir().convention(".")
    }

    @TaskAction
    void action() {
        Project project = getProject()
        Path projectDir = Paths.get(project.getLayout().getProjectDirectory().toString())

        FileTree sourceTree = project.fileTree(getSourceDir().get())
        Path sourceDir = sourceTree.getDir().toPath()

        FileTree targetTree = project.fileTree(getTargetDir().get())
        Path targetDir = targetTree.getDir().toPath()

        DirectoriesComparator comparator =
                new DirectoriesComparator(sourceDir, targetDir)

        DirectoriesDifferences differences = comparator.getDifferences()

        println("remainder in source:")
        println JsonOutput.prettyPrint(differences.getFilesOnlyInAAsString())

        println("remainder in target:")
        println JsonOutput.prettyPrint(differences.getFilesOnlyInBAsString())

        println("intersection:")
        println JsonOutput.prettyPrint(differences.getIntersectionAsString())

        println("modified files:")
        println JsonOutput.prettyPrint(differences.getModifiedFilesAsString())
    }
}

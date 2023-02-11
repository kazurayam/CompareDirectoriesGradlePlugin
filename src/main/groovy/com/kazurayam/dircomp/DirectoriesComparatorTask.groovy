package com.kazurayam.dircomp

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

    static Set<Path> collectRelativePaths(Path baseDir, FileTree fileTree) {
        Set<Path> relativePaths = new HashSet<Path>()
        fileTree.each { File f ->
            relativePaths.add(baseDir.relativize(f.toPath()))
        }
        return relativePaths
    }

    //@Input
    //abstract Property<ProjectLayout> getProjectLayout()

    @Input
    abstract Property<String> getSourceDir()

    @Input
    abstract Property<String> getTargetDir()

    //@Input
    //abstract Property<ConfigurableFileTree> getSourceFileTree()

    //@Input
    //abstract Property<ConfigurableFileTree> getTargetFileTree()

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
        Set<Path> sourceRelativePaths =
                collectRelativePaths(sourceDir, sourceTree)

        FileTree targetTree = project.fileTree(getTargetDir().get())
        Path targetDir = targetTree.getDir().toPath()
        Set<Path> targetRelativePaths =
                collectRelativePaths(targetDir, targetTree)

        DirectoriesComparator comparator =
                new DirectoriesComparator(projectDir,
                        sourceDir, sourceRelativePaths,
                        targetDir, targetRelativePaths)

        println("remainder in source:")
        comparator.getSourceRemainder().each { Path p ->
            println "    ${comparator.getSourceDirRelativeToProjectDir().resolve(p)}"
        }
        println()
        println("intersection:")
        comparator.getIntersection().each { Path p ->
            println "    <baseDir>/${p.toString()}"
        }
        println()
        println("remainder in target:")
        comparator.getTargetRemainder().each { Path p ->
            println "    ${comparator.getTargetDirRelativeToProjectDir().resolve(p)}"
        }
    }
}

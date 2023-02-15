package com.kazurayam.dircomp

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.ide.eclipse.model.Output

import java.nio.file.Path

abstract class DirectoriesComparatorTask extends DefaultTask {

    @Input
    abstract Property<String> getDirA()

    @Input
    abstract Property<String> getDirB()

    DirectoriesComparatorTask() {
        getDirA().convention(".")
        getDirB().convention(".")
    }

    @TaskAction
    void action() {
        FileTree sourceTree = project.fileTree(getDirA().get())
        Path sourceDir = sourceTree.getDir().toPath()

        FileTree targetTree = project.fileTree(getDirB().get())
        Path targetDir = targetTree.getDir().toPath()

        DirectoriesComparator comparator =
                new DirectoriesComparator(sourceDir, targetDir)

        DirectoriesDifferences differences = comparator.getDifferences()

        println JsonOutput.prettyPrint(differences.toJson())
    }
}

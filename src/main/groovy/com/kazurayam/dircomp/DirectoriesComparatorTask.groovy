package com.kazurayam.dircomp

import groovy.json.JsonOutput
import groovyjarjarantlr4.v4.codegen.model.OutputFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path
import java.nio.file.Paths

abstract class DirectoriesComparatorTask extends DefaultTask {

    @InputDirectory
    abstract Property<String> getDirA()

    @InputDirectory
    abstract Property<String> getDirB()

    @OutputFile
    abstract Property<String> getOutputFile()

    DirectoriesComparatorTask() {
        getDirA().convention(".")
        getDirB().convention(".")
        getOutputFile().convention("./differences.json")
    }

    @TaskAction
    void action() {
        FileTree sourceTree = project.fileTree(getDirA().get())
        Path sourceDir = sourceTree.getDir().toPath()

        FileTree targetTree = project.fileTree(getDirB().get())
        Path targetDir = targetTree.getDir().toPath()

        Path output = Paths.get(getOutputFile().get().toString())

        DirectoriesComparator comparator =
                new DirectoriesComparator(sourceDir, targetDir)

        DirectoriesDifferences differences = comparator.getDifferences()

        output.text = JsonOutput.prettyPrint(differences.serialize())
    }
}

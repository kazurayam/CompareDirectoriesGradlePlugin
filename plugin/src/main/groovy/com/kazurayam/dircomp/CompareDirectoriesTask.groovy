package com.kazurayam.dircomp

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path
import java.nio.file.Paths

abstract class CompareDirectoriesTask extends DefaultTask {

    @InputDirectory
    abstract DirectoryProperty getDirA()

    @InputDirectory
    abstract DirectoryProperty getDirB()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @OutputDirectory
    abstract DirectoryProperty getDiffDir()

    CompareDirectoriesTask() {
        getDirA().convention(project.layout.buildDirectory.dir("./dirA"))
        getDirB().convention(project.layout.buildDirectory.dir("./dirB"))
        getOutputFile().convention(project.layout.buildDirectory.file("./differences.json"))
        getDiffDir().convention(project.layout.buildDirectory.dir("./diff"))
        println "CompareDirectoriesTask has been instantiated"
    }

    @TaskAction
    void action() {
        println "entered into CompareDirectoriesTask#action()"
        Path baseDir = project.getLayout().getBuildDirectory()
                .get().getAsFile().toPath()

        FileTree fileTreeA = project.fileTree(getDirA().get())
        Path dirA = fileTreeA.getDir().toPath()

        FileTree fileTreeB = project.fileTree(getDirB().get())
        Path dirB = fileTreeB.getDir().toPath()

        Path outputFile = Paths.get(getOutputFile().get().toString())

        Path diffDir = Paths.get(getDiffDir().get().toString())

        println "going to instantiate CompareDirectoriesAction object"
        CompareDirectoriesAction actionObject =
                new CompareDirectoriesAction(baseDir, dirA, dirB,
                        outputFile, diffDir)
        int modifiedFiles = actionObject.action()

        println "number of modified files: ${modifiedFiles}"
    }
}

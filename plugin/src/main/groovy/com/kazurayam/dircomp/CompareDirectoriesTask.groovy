package com.kazurayam.dircomp

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
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
        if (!Files.exists(baseDir)) {
            throw new FileNotFoundException("${baseDir} is not found")
        }

        FileTree fileTreeA = project.fileTree(getDirA().get())
        Path dirA = fileTreeA.getDir().toPath()
        if (!Files.exists(dirA)) {
            throw new FileNotFoundException("${dirA} is not found")
        }

        FileTree fileTreeB = project.fileTree(getDirB().get())
        Path dirB = fileTreeB.getDir().toPath()
        if (!Files.exists(dirB)) {
            throw new FileNotFoundException("${dirB} is not found")
        }

        Path outputFile = Paths.get(getOutputFile().get().toString())
        Files.createDirectories(outputFile.getParent())

        Path diffDir = Paths.get(getDiffDir().get().toString())
        Files.createDirectories(diffDir)

        CompareDirectories comparator =
                new CompareDirectories(baseDir, dirA, dirB)
        // make the differences information
        DirectoriesDifferences differences = comparator.getDifferences()
        println "filesOnlyInA: ${differences.filesOnlyInA.size()} files"
        println "filesOnlyInB: ${differences.filesOnlyInB.size()} files"
        println "intersection: ${differences.intersection.size()} files"
        println "modifiedFiles: ${differences.modifiedFiles.size()} files"

        // write the differences.json
        outputFile.text = JsonOutput.prettyPrint(differences.serialize())

        // write unified-diff files of modified files
        differences.makeDiffFiles(diffDir)
    }
}

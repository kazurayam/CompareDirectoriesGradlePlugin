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
    }

    @TaskAction
    void action() {
        try {
            Path baseDir = project.buildDir.toPath()

            FileTree fileTreeA = project.fileTree(getDirA().get())
            Path pathDirA = fileTreeA.getDir().toPath()

            FileTree fileTreeB = project.fileTree(getDirB().get())
            Path pathDirB = fileTreeB.getDir().toPath()

            CompareDirectories comparator =
                    new CompareDirectories(baseDir, pathDirA, pathDirB)

            DirectoriesDifferences differences =
                    comparator.getDifferences()

            // write the differences.json
            Path output = Paths.get(getOutputFile().get().toString())
            output.text = JsonOutput.prettyPrint(differences.serialize())

            //
            Path diffDir = Paths.get(getDiffDir().get().toString())
            Files.createDirectories(diffDir)
            differences.makeDiffFiles(diffDir)

        } catch (Exception e) {
            e.printStackTrace()
            throw e
        }
    }
}

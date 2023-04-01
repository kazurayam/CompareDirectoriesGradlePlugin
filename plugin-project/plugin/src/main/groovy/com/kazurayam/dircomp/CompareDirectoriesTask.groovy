package com.kazurayam.dircomp

import com.github.difflib.patch.AbstractDelta
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import com.github.difflib.DiffUtils
import com.github.difflib.patch.Patch

abstract class CompareDirectoriesTask extends DefaultTask {

    @InputDirectory
    abstract Property<String> getDirA()

    @InputDirectory
    abstract Property<String> getDirB()

    @OutputFile
    abstract Property<String> getOutputFile()

    @OutputDirectory
    abstract Property<String> getDiffDir()

    CompareDirectoriesTask() {
        getDirA().convention("./dirA")
        getDirB().convention("./dirB")
        getOutputFile().convention("./differences.json")
        getDiffDir().convention("./diff")
    }

    @TaskAction
    void action() {
        try {
            FileTree sourceTree = project.fileTree(getDirA().get())
            Path sourceDir = sourceTree.getDir().toPath()

            FileTree targetTree = project.fileTree(getDirB().get())
            Path targetDir = targetTree.getDir().toPath()

            Path output = Paths.get(getOutputFile().get().toString())

            CompareDirectories comparator =
                    new CompareDirectories(sourceDir, targetDir)

            DirectoriesDifferences differences = comparator.getDifferences()

            // write the differences.json
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

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
        makeDiff(sourceDir, targetDir, differences, diffDir)
    }

    void makeDiff(Path sourceDir, Path targetDir, DirectoriesDifferences differences, Path diffDir) {
        differences.getModifiedFiles().forEach {modifiedFile ->
            List<String> textA = Files.readAllLines(sourceDir.resolve(modifiedFile))
            List<String> textB = Files.readAllLines(targetDir.resolve(modifiedFile))
            // generating diff information
            Patch<String> diff = DiffUtils.diff(textA, textB)
            // simple output the computed patch into file
            StringBuilder sb = new StringBuilder()
            for (AbstractDelta<String> delta : diff.getDeltas()) {
                sb.append(delta.toString())
                sb.append(System.lineSeparator())
            }
            String sourceDirName = sourceDir.getFileName().toString()
            String targetDirName = targetDir.getFileName().toString()
            Path diffOutputFile =
                    diffDir.resolve(sourceDirName + "_" + targetDirName)
                            .resolve(modifiedFile + ".diff.txt")
            Files.createDirectories(diffOutputFile.getParent())
            diffOutputFile.text = sb.toString()
        }
    }
}

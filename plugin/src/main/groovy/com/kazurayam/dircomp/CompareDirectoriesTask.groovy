package com.kazurayam.dircomp

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path

abstract class CompareDirectoriesTask extends DefaultTask {

    @InputFiles
    abstract Property<ConfigurableFileTree> getDirA()

    @InputFiles
    abstract Property<ConfigurableFileTree> getDirB()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @OutputDirectory
    abstract DirectoryProperty getDiffDir()

    @Input
    abstract ListProperty<String> getCharsetsToTry()

    CompareDirectoriesTask() {
        getDirA().convention(project.fileTree(project.layout.projectDirectory.dir("src")))
        getDirB().convention(project.fileTree(project.layout.projectDirectory.dir("src")))
        getOutputFile().convention(project.layout.buildDirectory.file("difference.json"))
        getDiffDir().convention(project.layout.buildDirectory.dir("diffDir"))
        getCharsetsToTry().convention([])
    }

    @TaskAction
    void action() {
        // compare 2 directories
        ConfigurableFileTree fileTreeA = getDirA().get()
        ConfigurableFileTree fileTreeB = getDirB().get()

        // do compare the 2 FileCollections retrieved out of the given FileTrees
        DirectoriesComparator comparator =
                new DirectoriesComparator(fileTreeA, fileTreeB)
        DirectoriesDifferences differences = comparator.getDifferences()

        // not only UTF-8, try additional Charsets
        if (getCharsetsToTry().get().size() > 0) {
            differences.addCharsetsToTry(getCharsetsToTry().get())
        }

        // write the summary json
        Path outputFile = getOutputFile().get().asFile.toPath()
        Files.createDirectories(outputFile.getParent())
        differences.serialize(outputFile)

        // create the unified-diff files, write them into to diffDir
        Path diffDir = getDiffDir().get().asFile.toPath()
        Files.createDirectories(diffDir)
        differences.makeDiffFiles(diffDir)

        // diagnose the result
        println "dirA: ${getDirA().get().getDir()}"
        println "dirB: ${getDirB().get().getDir()}"
        println "---------------------------------"
        println "filesOnlyInA: ${differences.filesOnlyInA.size()} files"
        println "filesOnlyInB: ${differences.filesOnlyInB.size()} files"
        println "intersection: ${differences.intersection.size()} files"
        println "modifiedFiles: ${differences.modifiedFiles.size()} files"

        //println "action() finished"
    }
}

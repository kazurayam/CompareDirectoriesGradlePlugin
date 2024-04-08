package com.kazurayam.dircomp

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
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

    CompareDirectoriesTask() {
        getDirA().convention(project.fileTree(project.layout.projectDirectory.dir("src")))
        getDirB().convention(project.fileTree(project.layout.projectDirectory.dir("src")))
        getOutputFile().convention(project.layout.buildDirectory.file("difference.json"))
        getDiffDir().convention(project.layout.buildDirectory.dir("diffDir"))
    }

    @TaskAction
    void action() {
        // compare 2 directories
        File baseDirA = getDirA().get().getDir()
        Set<File> filesA = getDirA().get().getFiles()
        File baseDirB = getDirB().get().getDir()
        Set<File> filesB = getDirB().get().getFiles()

        // do compare the 2 FileCollections retrieved out of the given FileTrees
        FileCollectionsComparator comparator =
                new FileCollectionsComparator(baseDirA, filesA, baseDirB, filesB)
        FileCollectionsDifferences differences = comparator.getDifferences()

        // write the summary json
        Path outputFile = getOutputFile().get().asFile.toPath()
        Files.createDirectories(outputFile.getParent())
        outputFile.text = differences.serialize()

        // create the unified-diff files, write them into to diffDir
        Path diffDir = getDiffDir().get().asFile.toPath()
        Files.createDirectories(diffDir)
        differences.makeDiffFiles(diffDir)

        // diagnose the result
        println "filesOnlyInA: ${differences.filesOnlyInA.size()} files"
        println "filesOnlyInB: ${differences.filesOnlyInB.size()} files"
        println "intersection: ${differences.intersection.size()} files"
        println "modifiedFiles: ${differences.modifiedFiles.size()} files"

        //println "action() finished"
    }
}

package com.kazurayam.dircomp

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

class CompareDirectoriesTask extends DefaultTask {

    private Logger logger = LoggerFactory.getLogger(CompareDirectoriesTask.class)

    @InputFiles
    ConfigurableFileTree dirA

    @InputFiles
    ConfigurableFileTree dirB

    @OutputFile
    File outputFile

    @OutputDirectory
    File diffDir

    CompareDirectoriesTask() {
        //println "enter CompareDirectoriesTask()"
        //getDirA().convention(project.fileTree("./dirA"))
        //getDirB().convention(project.fileTree("./dirB"))
        //outputFile.convention(project.layout.buildDirectory.file("./differences.json"))
        //diffDir.convention(project.layout.buildDirectory.dir("./diff"))
        //println "leave CompareDirectoriesTask()"
    }

    @TaskAction
    void action() {
        //println "action() started"

        Path outputFile = getOutputFile().toPath()
        Files.createDirectories(outputFile.getParent())

        Path diffDir = getDiffDir().toPath()
        Files.createDirectories(diffDir)

        // compare 2 directories
        File baseDirA = getDirA().getDir()
        FileCollection filesA = (FileCollection)getDirA()
        File baseDirB = getDirB().getDir()
        FileCollection filesB = (FileCollection)getDirB()
        //
        FileCollectionsComparator comparator =
                new FileCollectionsComparator(baseDirA, filesA, baseDirB, filesB)
        FileCollectionsDifferences differences = comparator.getDifferences()
        println "filesOnlyInA: ${differences.filesOnlyInA.size()} files"
        println "filesOnlyInB: ${differences.filesOnlyInB.size()} files"
        println "intersection: ${differences.intersection.size()} files"
        println "modifiedFiles: ${differences.modifiedFiles.size()} files"

        // write the differences.json
        outputFile.text = JsonOutput.prettyPrint(differences.serialize())

        // write unified-diff files of modified files
        differences.makeDiffFiles(diffDir)

        //println "action() finished"
    }
}

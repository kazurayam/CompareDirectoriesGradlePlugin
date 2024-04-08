package com.kazurayam.dircomp

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

abstract class CompareDirectoriesTask extends DefaultTask {

    private Logger logger = LoggerFactory.getLogger(CompareDirectoriesTask.class)

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
        FileCollection filesA = (FileCollection)getDirA()
        File baseDirB = getDirB().get().getDir()
        FileCollection filesB = (FileCollection)getDirB()

        // do compare the 2 FileCollections retrieved out of the given FileTrees
        FileCollectionsComparator comparator =
                new FileCollectionsComparator(baseDirA, filesA, baseDirB, filesB)
        FileCollectionsDifferences differences = comparator.getDifferences()

        // write the summary json
        Path outputFile = getOutputFile().get().asFile.toPath()
        Files.createDirectories(outputFile.getParent())
        outputFile.text = JsonOutput.prettyPrint(differences.serialize())

        // create the unified-diff files, write them into to diffDir
        Path diffDir = getDiffDir().get().asFile().toPath()
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

package com.kazurayam.dircomp

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class CompareDirectoriesTask extends DefaultTask {

    private Logger logger = LoggerFactory.getLogger(CompareDirectoriesTask.class)

    @Input
    abstract FileCollection getDirA()

    @Input
    abstract FileCollection getDirB()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @OutputDirectory
    abstract DirectoryProperty getDiffDir()

    CompareDirectoriesTask() {
        //println "enter CompareDirectoriesTask()"
        //getDirA().convention(project.layout.projectDirectory.dir("./dirA"))
        //getDirB().convention(project.layout.projectDirectory.dir("./dirB"))
        getOutputFile().convention(project.layout.buildDirectory.file("./differences.json"))
        getDiffDir().convention(project.layout.buildDirectory.dir("./diff"))
        //println "leave CompareDirectoriesTask()"
    }

    @TaskAction
    void action() {
        //println "action() started"
        Path baseDir = project.getLayout().getBuildDirectory()
                .get().getAsFile().toPath()
        if (!Files.exists(baseDir)) {
            throw new FileNotFoundException("${baseDir} is not found")
        }

        if (!Files.exists(getDirA().getDir().toPath())) {
            throw new FileNotFoundException("${dirA} is not found")
        }

        if (!Files.exists(getDirB().getDir().toPath())) {
            throw new FileNotFoundException("${dirB} is not found")
        }

        Path outputFile = Paths.get(getOutputFile().get().toString())
        Files.createDirectories(outputFile.getParent())

        Path diffDir = Paths.get(getDiffDir().get().toString())
        Files.createDirectories(diffDir)

        // compare 2 directories
        DirectoriesComparator comparator =
                new DirectoriesComparator(baseDir, dirA, dirB)
        DirectoriesDifferences differences = comparator.getDifferences()
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

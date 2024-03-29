package com.kazurayam.dircomp

import groovy.json.JsonOutput

import java.nio.file.Files
import java.nio.file.Path

class CompareDirectoriesAction {

    private Path baseDir
    private Path dirA
    private Path dirB
    private Path outputFile
    private Path diffDir

    CompareDirectoriesAction(Path baseDir,
                             Path dirA,
                             Path dirB,
                             Path outputFile,
                             Path diffDir) {
        this.baseDir = baseDir
        this.dirA = dirA
        this.dirB = dirB
        this.outputFile = outputFile
        this.diffDir = diffDir
        if (!Files.exists(baseDir)) {
            throw new FileNotFoundException("${baseDir} is not found")
        }
        if (!Files.exists(dirA)) {
            throw new FileNotFoundException("${dirA} is not found")
        }
        if (!Files.exists(dirB)) {
            throw new FileNotFoundException("${dirB} is not found")
        }
        Files.createDirectories(outputFile.getParent())
        Files.createDirectories(diffDir)
    }

    int action() {
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

        // return the number of modified files
        return differences.modifiedFiles.size()
    }
}

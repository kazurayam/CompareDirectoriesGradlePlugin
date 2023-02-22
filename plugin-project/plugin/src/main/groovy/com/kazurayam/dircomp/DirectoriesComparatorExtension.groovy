package com.kazurayam.dircomp

import org.gradle.api.file.RegularFile

class DirectoriesComparatorExtension {

    public String dirA
    public String dirB
    public String outputFile

    String getDirA() {
        return dirA
    }

    String getDirB() {
        return dirB
    }

    String getOutputFile() {
        return outputFile
    }
}

package com.kazurayam.dircomp

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

interface CompareDirectoriesExtension {
    DirectoryProperty getDirA()
    DirectoryProperty getDirB()
    RegularFileProperty getOutputFile()
    DirectoryProperty getDiffDir()
}

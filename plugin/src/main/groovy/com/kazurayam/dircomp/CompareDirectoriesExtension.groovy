package com.kazurayam.dircomp

import org.gradle.api.Project
import org.gradle.api.provider.Property

class CompareDirectoriesExtension {

    Property<Object> dirA
    Property<Object> dirB
    Property<Object> outputFile
    Property<Object> diffDir

    CompareDirectoriesExtension(Project project) {
        this.dirA = project.objects.property(Object)
        this.dirB = project.objects.property(Object)
        this.outputFile = project.objects.property(Object)
        this.diffDir = project.objects.property(Object)
    }
}

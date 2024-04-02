package com.kazurayam.dircomp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree

class CompareDirectoriesPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // add the 'compareDirectories' extension object into the project
        def ext = project.extensions.create("compareDirectories", CompareDirectoriesExtension)

        // create the 'compareDirectories' task and register it into the project
        project.tasks.register("compareDirectories", CompareDirectoriesTask) { task ->
            task.dirA = (ConfigurableFileTree)ext.dirA.get()
            task.dirB = (ConfigurableFileTree)ext.dirB.get()
            task.outputFile = (File)ext.outputFile.get()
            task.diffDir = (File)ext.diffDir.get()
        }
    }
}

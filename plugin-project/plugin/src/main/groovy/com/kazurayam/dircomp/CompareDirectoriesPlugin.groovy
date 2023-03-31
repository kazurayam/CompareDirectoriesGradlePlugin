package com.kazurayam.dircomp

import org.gradle.api.Plugin
import org.gradle.api.Project

class CompareDirectoriesPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // add the 'compareDirectories' extension object
        CompareDirectoriesExtension extension =
                project.getExtensions()
                        .create("compareDirectories",
                                CompareDirectoriesExtension.class)
        // create the 'compareDirectories' task
        project.getTasks().register("compareDirectories",
                CompareDirectoriesTask.class,
                task -> {
                    task.getDirA().set(extension.getDirA())
                    task.getDirB().set(extension.getDirB())
                    task.getOutputFile().set(extension.getOutputFile())
                    task.getDiffDir().set(extension.getDiffDir())
        })
    }
}

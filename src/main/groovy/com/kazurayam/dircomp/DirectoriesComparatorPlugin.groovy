package com.kazurayam.dircomp;

import org.gradle.api.Plugin
import org.gradle.api.Project

class DirectoriesComparatorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // add the 'compareDirectories' extension object
        DirectoriesComparatorExtension extension =
                project.getExtensions()
                        .create("compareDirectories",
                                DirectoriesComparatorExtension.class)
        // create the 'compareDirectories' task
        project.getTasks().register("compareDirectories",
                DirectoriesComparatorTask.class,
                task -> {
            task.getProjectLayout().set(project.getLayout())
            task.getSourceDir().set(extension.getSourceDir())
            task.getTargetDir().set(extension.getTargetDir())
            task.getSourceFileTree().set(
                    project.fileTree(extension.getSourceDir()))
            task.getTargetFileTree().set(
                    project.fileTree(extension.getTargetDir()))
        })
    }
}

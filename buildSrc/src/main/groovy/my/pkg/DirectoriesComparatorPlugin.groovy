package my.pkg;

import org.gradle.api.Plugin
import org.gradle.api.Project

public class DirectoriesComparatorPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // add the 'compareDirectories' extension object
        DirectoriesComparatorExtension extension =
                project.getExtensions()
                        .create("compareDirectories",
                                DirectoriesComparatorExtension.class)
        // create the 'compareDirectories' task
        project.getTasks().register("compareDirectories",
                DirectoriesComparator.class, task -> {
            task.getSourceDir().set(extension.getSourceDir())
            task.getTargetDir().set(extension.getTargetDir())
            task.getSourceFileTree().set(
                    project.fileTree(extension.getSourceDir()))
            task.getTargetFileTree().set(
                    project.fileTree(extension.getTargetDir()))
        })
    }
}

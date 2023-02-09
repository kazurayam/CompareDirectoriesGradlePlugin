package my.pkg;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DirectoriesComparatorPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // Add the 'directoriesToCompare' extension object
        project.extensions.create("compareDirectories", DirectoriesComparatorExtension);
        //
        project.tasks.create("compareDirectories") {
            doLast {
                println("sourceDir: ${project.extensions.getByType(DirectoriesComparatorExtension.class).sourceDir}")
                println("targetDir: ${project.extensions.getByType(DirectoriesComparatorExtension.class).targetDir}")
            }
        }
    }
}

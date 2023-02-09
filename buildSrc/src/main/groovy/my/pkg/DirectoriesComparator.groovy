package my.pkg

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract public class DirectoriesComparator extends DefaultTask {

    @Input
    abstract public Property<String> getSourceDir();

    @Input
    abstract public Property<String> getTargetDir();

    public DirectoriesComparator() {
        getSourceDir().convention(".")
        getTargetDir().convention(".")
    }

    @TaskAction
    public void action() {
        println("sourceDir: ${getSourceDir().get()}")
        println("targetDir: ${getTargetDir().get()}")
    }
}

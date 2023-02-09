package my.pkg

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class DirectoriesComparator extends DefaultTask {
    @TaskAction
    public void run() {
        System.out.println("Hello from task " + getPath() + "!");
    }
}

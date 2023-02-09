package my.pkg

import java.nio.file.Files
import java.nio.file.Path

class DirectoriesComparator {

    private final Path projectDir
    private final Path sourceDir
    private final Path targetDir
    private final Set<Path> sourceRelativePaths
    private final Set<Path> targetRelativePaths

    private Set<Path> sourceRemainder
    private Set<Path> targetRemainder
    private Set<Path> intersection


    DirectoriesComparator(Path projectDir,
                          Path sourceDir, Set<Path> sourceRelativePaths,
                          Path targetDir, Set<Path> targetRelativePaths) {
        Objects.requireNonNull(projectDir)
        Objects.requireNonNull(sourceDir)
        Objects.requireNonNull(sourceRelativePaths)
        Objects.requireNonNull(targetDir)
        Objects.requireNonNull(targetRelativePaths)
        assert Files.exists(projectDir)
        assert Files.exists(sourceDir)
        assert Files.exists(targetDir)
        //
        this.projectDir = projectDir.toAbsolutePath().normalize()
        this.sourceDir = sourceDir.toAbsolutePath().normalize()
        this.targetDir = targetDir.toAbsolutePath().normalize()
        this.sourceRelativePaths = sourceRelativePaths
        this.targetRelativePaths = targetRelativePaths

        //
        sourceRemainder = new HashSet<Path>(this.sourceRelativePaths)
        sourceRemainder.removeAll(this.targetRelativePaths)
        //
        targetRemainder = new HashSet<Path>(this.targetRelativePaths)
        targetRemainder.removeAll(this.sourceRelativePaths)
        //
        intersection = new HashSet<Path>(this.sourceRelativePaths)
        intersection.retainAll(this.targetRelativePaths)
    }


    Path getProjectDir() {
        return this.projectDir
    }

    Path getSourceDir() {
        return this.sourceDir
    }

    Path getSourceDirRelativeToProjectDir() {
        return this.projectDir.relativize(getSourceDir())
    }

    Path getTargetDir() {
        return this.targetDir
    }

    Path getTargetDirRelativeToProjectDir() {
        return this.projectDir.relativize(getTargetDir())
    }

    List<Path> getSourceRemainder() {
        List<Path> list = new ArrayList<Path>(sourceRemainder)
        return list.sort()   // sort the list alphabetically
    }

    List<Path> getIntersection() {
        List<Path> list = new ArrayList<Path>(intersection)
        return list.sort()
    }

    List<Path> getTargetRemainder() {
        List<Path> list = new ArrayList<Path>(targetRemainder)
        return list.sort()
    }
}

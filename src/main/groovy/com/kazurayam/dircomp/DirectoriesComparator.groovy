package com.kazurayam.dircomp

import java.nio.file.Files
import java.nio.file.Path

import java.security.MessageDigest

class DirectoriesComparator {

    private final MessageDigest digester = MessageDigest.getInstance('SHA')

    private final Path projectDir
    private final Path sourceDir
    private final Path targetDir

    private final Set<Path> sourceRelativePaths
    private final Set<Path> targetRelativePaths

    private final DirectoriesDifferences differences

    DirectoriesComparator(Path sourceDir,
                          Path targetDir) {
        Objects.requireNonNull(sourceDir)
        Objects.requireNonNull(targetDir)
        assert Files.exists(sourceDir)
        assert Files.exists(targetDir)
        //
        this.sourceDir = sourceDir.toAbsolutePath().normalize()
        this.targetDir = targetDir.toAbsolutePath().normalize()
        //
        this.sourceRelativePaths = FileTreeBuilder.scan(sourceDir)
        this.targetRelativePaths = FileTreeBuilder.scan(targetDir)

        //
        Set<Path> sourceRemainder = new HashSet<Path>(this.sourceRelativePaths)
        sourceRemainder.removeAll(this.targetRelativePaths)

        //
        Set<Path> targetRemainder = new HashSet<Path>(this.targetRelativePaths)
        targetRemainder.removeAll(this.sourceRelativePaths)

        // intersection of dirA and dirB
        Set<Path> intersection = new HashSet<Path>(this.sourceRelativePaths)
        intersection.retainAll(this.targetRelativePaths)

        // find modified files
        Set<Path> modifiedFiles = new HashSet<Path>()
        for (Path subPath : intersection) {
            Path fileA = sourceDir.resolve(subPath)
            Path fileB = targetDir.resolve(subPath)
            if (different(fileA, fileB)) {
                modifiedFiles.add(subPath)
            }
        }
        differences =
                new DirectoriesDifferences(sourceRemainder,
                        targetRemainder, intersection, modifiedFiles)
    }

    Path getSourceDir() {
        return this.sourceDir
    }

    Path getTargetDir() {
        return this.targetDir
    }

    DirectoriesDifferences getDifferences() {
        return differences
    }

    private boolean different(Path fileA, Path fileB) {
        hashFile(fileA) != hashFile(fileB)
    }

    private byte[] hashFile(Path filePath) {
        digester.digest(filePath.toFile().bytes)
    }

}

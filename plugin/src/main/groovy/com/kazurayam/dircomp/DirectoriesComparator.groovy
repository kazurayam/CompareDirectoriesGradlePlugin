package com.kazurayam.dircomp


import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class DirectoriesComparator {

    private final MessageDigest digester = MessageDigest.getInstance('SHA')

    private Path baseDir
    private Path directoryA
    private Path directoryB
    private DirectoriesDifferences differences

    DirectoriesComparator(Path baseDir,
                          Path dirA,
                          Path dirB) {
        Objects.requireNonNull(baseDir)
        Objects.requireNonNull(dirA)
        Objects.requireNonNull(dirB)
        assert Files.exists(baseDir)
        assert Files.exists(dirA)
        assert Files.exists(dirB)
        //
        this.baseDir = baseDir
        this.directoryA = dirA.toAbsolutePath().normalize()
        this.directoryB = dirB.toAbsolutePath().normalize()
        //
        Set<String> subPathsA =
                new DirectoryScanner(directoryA).scan().getSubPaths()
        Set<String> subPathsB =
                new DirectoryScanner(directoryB).scan().getSubPaths()
        //
        Set<String> filesOnlyInA = new HashSet<String>(subPathsA)
        filesOnlyInA.removeAll(subPathsB)
        //
        Set<String> filesOnlyInB = new HashSet<String>(subPathsB)
        filesOnlyInB.removeAll(subPathsA)
        // intersection of dirA and dirB
        Set<String> intersection = new HashSet<String>(subPathsA)
        intersection.retainAll(subPathsB)
        // find modified files
        Set<String> modifiedFiles = new HashSet<String>()
        for (String subPath : intersection) {
            Path fileA = dirA.resolve(subPath)
            Path fileB = dirB.resolve(subPath)
            if (different(fileA, fileB)) {
                modifiedFiles.add(subPath)
            }
        }
        differences =
                new DirectoriesDifferences(dirA, dirB,
                        filesOnlyInA,
                        filesOnlyInB,
                        intersection,
                        modifiedFiles)
    }

    Path getDirA() {
        return this.directoryA
    }

    Path getDirB() {
        return this.directoryB
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

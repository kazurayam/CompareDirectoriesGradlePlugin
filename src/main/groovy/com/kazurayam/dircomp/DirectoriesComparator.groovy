package com.kazurayam.dircomp

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class DirectoriesComparator {

    private final MessageDigest digester = MessageDigest.getInstance('SHA')

    private final Path directoryA
    private final Path directoryB
    private final DirectoriesDifferences differences

    DirectoriesComparator(Path dirA,
                          Path dirB) {
        Objects.requireNonNull(dirA)
        Objects.requireNonNull(dirB)
        assert Files.exists(dirA)
        assert Files.exists(dirB)
        //
        directoryA = dirA.toAbsolutePath().normalize()
        directoryB = dirB.toAbsolutePath().normalize()
        //
        Set<Path> subPathsA = FileTreeBuilder.scan(directoryA)
        Set<Path> subPathsB = FileTreeBuilder.scan(directoryB)

        //
        Set<Path> filesOnlyInA = new HashSet<Path>(subPathsA)
        filesOnlyInA.removeAll(subPathsB)

        //
        Set<Path> filesOnlyInB = new HashSet<Path>(subPathsB)
        filesOnlyInB.removeAll(subPathsA)

        // intersection of dirA and dirB
        Set<Path> intersection = new HashSet<Path>(subPathsA)
        intersection.retainAll(subPathsB)

        // find modified files
        Set<Path> modifiedFiles = new HashSet<Path>()
        for (Path subPath : intersection) {
            Path fileA = dirA.resolve(subPath)
            Path fileB = dirB.resolve(subPath)
            if (different(fileA, fileB)) {
                modifiedFiles.add(subPath)
            }
        }
        differences =
                new DirectoriesDifferences(filesOnlyInA, filesOnlyInB,
                        intersection, modifiedFiles)
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

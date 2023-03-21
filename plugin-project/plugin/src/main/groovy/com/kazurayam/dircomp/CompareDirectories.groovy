package com.kazurayam.dircomp

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class CompareDirectories {

    private final MessageDigest digester = MessageDigest.getInstance('SHA')

    private Path directoryA
    private Path directoryB
    private DirectoriesDifferences differences

    CompareDirectories(Path dirA,
                       Path dirB) {
        Objects.requireNonNull(dirA)
        Objects.requireNonNull(dirB)
        assert Files.exists(dirA)
        assert Files.exists(dirB)
        //
        directoryA = dirA.toAbsolutePath().normalize()
        directoryB = dirB.toAbsolutePath().normalize()
        //
        Set<String> subPathsA = FileTreeBuilder.scan(directoryA)
        Set<String> subPathsB = FileTreeBuilder.scan(directoryB)

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

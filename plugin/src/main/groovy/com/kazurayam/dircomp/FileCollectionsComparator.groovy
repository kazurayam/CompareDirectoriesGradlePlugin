package com.kazurayam.dircomp

import org.gradle.api.file.FileCollection

import java.nio.file.Path
import java.security.MessageDigest
import java.util.stream.Collectors

class FileCollectionsComparator {

    private final MessageDigest digester = MessageDigest.getInstance('SHA')

    private final Path dirA
    private final Set<Path> collectionA
    private final Path dirB
    private final Set<Path> collectionB
    private FileCollectionsDifferences differences

    FileCollectionsComparator(File dirA,
                              FileCollection collectionA,
                              File dirB,
                              FileCollection collectionB) {
        this(dirA, collectionA as Set<File>, dirB, collectionB as Set<File>)
    }

    FileCollectionsComparator(File dirA,
                              Set<File> collectionA,
                              File dirB,
                              Set<File> collectionB) {
        this(dirA.toPath(),
                collectionA.stream().map(f -> f.toPath()).collect(Collectors.toSet()),
                dirB.toPath(),
                collectionB.stream().map(f -> f.toPath()).collect(Collectors.toSet())
                )
    }

    FileCollectionsComparator(Path dirA,
                              Set<Path> collectionA,
                              Path dirB,
                              Set<Path> collectionB) {
        this.dirA = dirA
        this.collectionA = collectionA
        this.dirB = dirB
        this.collectionB = collectionB
        doCompare()
    }

    void doCompare() {
        Set<String> subPathsA = toSubPaths(dirA, collectionA)
        Set<String> subPathsB = toSubPaths(dirB, collectionB)

        // make a set of sub-paths that exists only in the dirA
        Set<String> filesOnlyInA = new HashSet<String>(subPathsA)
        filesOnlyInA.removeAll(subPathsB)

        // make a set of sub-paths that exists only in the dirB
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
                new FileCollectionsDifferences(dirA, dirB,
                        filesOnlyInA,
                        filesOnlyInB,
                        intersection,
                        modifiedFiles)
    }

    FileCollectionsDifferences getDifferences() {
        return this.differences
    }

    static Set<String> toSubPaths(Path dir, List<Path> paths) {
        Set<Path> set = new HashSet<>(paths)
        return toSubPaths(dir, set)
    }

    static Set<String> toSubPaths(Path dir, Set<Path> paths) {
        Set<String> set = new HashSet<>()
        paths.each {path ->
            set.add(dir.relativize(path).toString())
        }
        return set
    }

    private boolean different(Path fileA, Path fileB) {
        hashFile(fileA) != hashFile(fileB)
    }

    private byte[] hashFile(Path filePath) {
        digester.digest(filePath.toFile().bytes)
    }
}

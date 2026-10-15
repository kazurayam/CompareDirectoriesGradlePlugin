package com.kazurayam.dircomp

import org.gradle.api.file.ConfigurableFileTree

import java.nio.file.Path
import java.security.MessageDigest
import java.util.stream.Collectors
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DirectoriesComparator {

    private static final Logger logger = LoggerFactory.getLogger(DirectoriesComparator.class)

    private static final MessageDigest digester = MessageDigest.getInstance('SHA')

    private final Path dirA
    private final Set<Path> collectionA
    private final Path dirB
    private final Set<Path> collectionB
    private DirectoriesDifferences differences

    DirectoriesComparator(ConfigurableFileTree fileTreeA,
                          ConfigurableFileTree fileTreeB) {
        this(fileTreeA.getDir(), fileTreeA.files, fileTreeB.getDir(), fileTreeB.files)
    }

    DirectoriesComparator(File dirA,
                          Set<File> collectionA,
                          File dirB,
                          Set<File> collectionB) {
        this(dirA.toPath(),
                collectionA.stream().map(f -> f.toPath()).collect(Collectors.toSet()),
                dirB.toPath(),
                collectionB.stream().map(f -> f.toPath()).collect(Collectors.toSet())
                )
    }

    DirectoriesComparator(Path dirA,
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
                new DirectoriesDifferences(dirA, dirB,
                        filesOnlyInA,
                        filesOnlyInB,
                        intersection,
                        modifiedFiles)
    }

    DirectoriesDifferences getDifferences() {
        return this.differences
    }

    static Set<String> toSubPaths(Path dir, List<Path> paths) {
        Set<Path> set = new HashSet<>(paths)
        return toSubPaths(dir, set)
    }

    static Set<String> toSubPaths(Path dir, Set<Path> paths) {
        Set<String> set = new HashSet<>()
        paths.each {path ->

            //System.out.println("dir : " + dir)
            //System.out.println("path: " + path)

            set.add(dir.relativize(path).toString())
        }
        return set
    }

    /**
     * calculate the message digest of fileA and fileB, return true
     * if the values differ. return false if the values are equal.
     *
     * At first, the method assumes that fileA and fileB are text.
     * Will read fileA into a string and take the message digest.
     * Will read fileB into another string and take the message digest.
     * Reading a text file will drop all CR characters at the trail of lines.
     * If the message digests differ, returns true. Otherwise return false.
     *
     * If reading the files as text raised an Exception, then the method
     * will regard the file(s) are binary. So the method just compare them
     * by message digest of files.
     *
     * @param fileA
     * @param fileB
     * @return
     */
    protected static boolean different(Path fileA, Path fileB) {
        boolean result;
        try {
            String contentA = readAllLines(fileA)  // strip CR+LF -> LF
            String contentB = readAllLines(fileB)
            //println "hashString(contentA)=" + hashString(contentA)
            //println "hashString(contentB)=" + hashString(contentB)
            result = hashString(contentA) != hashString(contentB)
        } catch (Exception ignored) {
            result = hashFile(fileA) != hashFile(fileB)
        }
        return result;
    }

    /**
     * calculate the message digest of the file
     * @param filePath
     * @return
     */
    private static byte[] hashFile(Path filePath) {
        digester.digest(filePath.toFile().bytes)
    }

    private static byte[] hashString(String string) {
        digester.digest(string.bytes);
    }

    private static String[] readAllLines(Path file) {
        InputStream is = new FileInputStream(file.toFile())
        BufferedReader br = new BufferedReader(new InputStreamReader(is))
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n")   // change CR+LF to LF
        }
        return sb.toString()
    }
}

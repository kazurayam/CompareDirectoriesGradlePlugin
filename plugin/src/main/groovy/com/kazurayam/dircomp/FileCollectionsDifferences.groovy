package com.kazurayam.dircomp

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.MalformedInputException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

/**
 * Data object that contains information of the differences between 2 directories
 */
class FileCollectionsDifferences {

    private static Logger logger = LoggerFactory.getLogger(FileCollectionsDifferences.class)

    private Path dirA
    private Path dirB

    /**
     * The files found only in the first directory (A)
     */
    private Set<String> filesOnlyInA

    /**
     * The files found only in the second directory (B)
     */
    private Set<String> filesOnlyInB

    /**
     * The files found in both directories regardless if the content is same or not
     */
    private Set<String> intersection

    /**
     * The files existing in both directories but have different content
     */
    private Set<String> modifiedFiles

    /**
     * com.fasterxml.jackson.databind requires the default constructor without args
     */
    FileCollectionsDifferences() {
        this.dirA = null;
        this.dirB = null;
        this.filesOnlyInA = new HashSet<>();
        this.filesOnlyInB = new HashSet<>();
        this.intersection = new HashSet<>();
        this.modifiedFiles = new HashSet<>();
    }

    FileCollectionsDifferences(
            Path dirA,
            Path dirB,
            Set<String> filesOnlyInA,
            Set<String> filesOnlyInB,
            Set<String> intersection,
            Set<String> modifiedFiles) {
        this.dirA = dirA.normalize()
        this.dirB = dirB.normalize()
        this.filesOnlyInA = filesOnlyInA
        this.filesOnlyInB = filesOnlyInB
        this.intersection = intersection
        this.modifiedFiles = modifiedFiles
    }

    Path getDirA() {
        return dirA
    }

    Path getDirB() {
        return dirB
    }

    void setFilesOnlyInA(Collection<String> filesOnlyInA) {
        this.filesOnlyInA = new HashSet<>(filesOnlyInA);
    }

    List<String> getFilesOnlyInA() {
        return filesOnlyInA.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    void setFilesOnlyInB(Collection<String> filesOnlyInB) {
        this.filesOnlyInB = new HashSet<>(filesOnlyInB);
    }

    List<String> getFilesOnlyInB() {
        return filesOnlyInB.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    void setIntersection(Collection<String> intersection) {
        this.intersection = new HashSet<>(intersection);
    }

    List<String> getIntersection() {
        return intersection.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    void setModifiedFiles(Collection<String> modifiedFiles) {
        this.modifiedFiles = new HashSet<>(modifiedFiles);
    }

    List<String> getModifiedFiles() {
        return modifiedFiles.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    String toString() {
        return serialize();
    }

    String serialize() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            return jsonResult;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static FileCollectionsDifferences deserialize(File json) {
        return deserialize(json.toPath());
    }

    static FileCollectionsDifferences deserialize(Path jsonFile) {
        try {
            String json = String.join("\n", Files.readAllLines(jsonFile));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, FileCollectionsDifferences.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return number of the diff files created
     */
    int makeDiffFiles(Path diffDir) {
        Objects.requireNonNull(diffDir)
        assert Files.exists(diffDir)
        int result = 0
        this.getModifiedFiles().forEach {relativePath ->
            //println "* relativePath: " + relativePath
            //logger.info("* relative path: " + relativePath)
            try {
                Path fileA = this.getDirA().resolve(relativePath).toAbsolutePath()
                Path fileB = this.getDirB().resolve(relativePath).toAbsolutePath()
                List<String> textA = readAllLines(fileA)
                List<String> textB = readAllLines(fileB)
                // generating diff information
                Patch<String> diff = DiffUtils.diff(textA, textB)

                // generating unified diff format
                String relativePathA = dirA.toString() + "/" + relativePath
                String relativePathB = dirB.toString() + "/" + relativePath
                List<String> unifiedDiff =
                        UnifiedDiffUtils.generateUnifiedDiff(
                                relativePathA, relativePathB, textA, diff, 0)

                /*
                logger.debug("unifiedDiff.size()=" + unifiedDiff.size())
                unifiedDiff.each {
                    logger.trace("========== " + relativePath + " ==========")
                    logger.trace(it)
                }
                 */

                //
                String dirAName = this.getDirA().getFileName().toString()
                String dirBName = this.getDirB().getFileName().toString()
                Path diffOutputFile =
                        diffDir.resolve(dirAName + "_" + dirBName)
                                .resolve(relativePath)
                Files.createDirectories(diffOutputFile.getParent())
                BufferedWriter br =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(diffOutputFile.toFile()),
                                        "UTF-8"))
                // print the unified diff into file
                for (String line : unifiedDiff) {
                    br.println(line)
                }
                br.flush()
                br.close()
                result += 1
            } catch (Exception e) {
                logger.warn(e.getMessage())
            }
        }
        return result
    }

    static List<String> readAllLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>()
        try {
            lines = new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8))
        } catch (MalformedInputException e) {
            String msg = "Failed to read " + file.toString() + " as a text in UTF-8"
            lines.add(msg)
            logger.warn(msg)
        }
        return lines
    }
}

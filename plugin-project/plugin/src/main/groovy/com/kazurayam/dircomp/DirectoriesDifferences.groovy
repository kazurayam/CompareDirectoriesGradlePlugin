package com.kazurayam.dircomp

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.difflib.DiffUtils
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.Patch

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class DirectoriesDifferences {

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

    private Set<String> intersection

    /**
     * The files existing in both directories but have different content
     */
    private Set<String> modifiedFiles

    DirectoriesDifferences() {
        this.dirA = null;
        this.dirB = null;
        this.filesOnlyInA = new HashSet<>();
        this.filesOnlyInB = new HashSet<>();
        this.intersection = new HashSet<>();
        this.modifiedFiles = new HashSet<>();
    }

    DirectoriesDifferences(Path dirA, Path dirB,
                                  Set<String> filesOnlyInA,
                                  Set<String> filesOnlyInB,
                                  Set<String> intersection,
                                  Set<String> modifiedFiles) {
        this.dirA = dirA.normalize();
        this.dirB = dirB.normalize();
        this.filesOnlyInA = filesOnlyInA;
        this.filesOnlyInB = filesOnlyInB;
        this.intersection = intersection;
        this.modifiedFiles = modifiedFiles;
    }

    void setDirA(Path dirA) {
        this.dirA = dirA;
    }

    Path getDirA() {
        return dirA;
    }

    void setDirB(Path dirB) {
        this.dirB = dirB;
    }

    Path getDirB() {
        return dirB;
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

    static DirectoriesDifferences deserialize(File json) {
        return deserialize(json.toPath());
    }

    static DirectoriesDifferences deserialize(Path jsonFile) {
        try {
            String json = String.join("\n", Files.readAllLines(jsonFile));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, DirectoriesDifferences.class);
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
        this.getModifiedFiles().forEach {modifiedFile ->
            //println "modifiedFile: " + modifiedFile.toString()
            try {
                List<String> textA = Files.readAllLines(this.getDirA().resolve(modifiedFile))
                List<String> textB = Files.readAllLines(this.getDirB().resolve(modifiedFile))
                // generating diff information
                Patch<String> diff = DiffUtils.diff(textA, textB)
                // simple output the computed patch into file
                StringBuilder sb = new StringBuilder()
                for (AbstractDelta<String> delta : diff.getDeltas()) {
                    sb.append(delta.toString())
                    sb.append(System.lineSeparator())
                }
                String sourceDirName = this.getDirA().getFileName().toString()
                String targetDirName = this.getDirB().getFileName().toString()
                Path diffOutputFile =
                        diffDir.resolve(sourceDirName + "_" + targetDirName)
                                .resolve(URLEncoder.encode(modifiedFile.toString(), "UTF-8"))
                Files.createDirectories(diffOutputFile.getParent())
                diffOutputFile.text = sb.toString()
                //println "diffOutputFile=" + diffOutputFile.toString()
                result += 1
            } catch (Exception e) {
                e.printStackTrace()
                throw e
            }
        }
        return result
    }
}

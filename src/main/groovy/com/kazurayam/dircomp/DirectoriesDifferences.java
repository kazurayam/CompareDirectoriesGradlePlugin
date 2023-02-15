package com.kazurayam.dircomp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DirectoriesDifferences {

    private Path dirA;

    private Path dirB;

    /**
     * The files found only in the first directory (A)
     */
    private Set<String> filesOnlyInA;

    /**
     * The files found only in the second directory (B)
     */
    private Set<String> filesOnlyInB;

    private Set<String> intersection;

    /**
     * The files existing in both directories but have different content
     */
    private Set<String> modifiedFiles;

    public DirectoriesDifferences() {
        this.dirA = null;
        this.dirB = null;
        this.filesOnlyInA = new HashSet<>();
        this.filesOnlyInB = new HashSet<>();
        this.intersection = new HashSet<>();
        this.modifiedFiles = new HashSet<>();
    }

    public DirectoriesDifferences(Path dirA, Path dirB,
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

    public void setDirA(Path dirA) {
        this.dirA = dirA;
    }

    public Path getDirA() {
        return dirA;
    }

    public void setDirB(Path dirB) {
        this.dirB = dirB;
    }

    public Path getDirB() {
        return dirB;
    }

    public void setFilesOnlyInA(Collection<String> filesOnlyInA) {
        this.filesOnlyInA = new HashSet<>(filesOnlyInA);
    }

    public List<String> getFilesOnlyInA() {
        return filesOnlyInA.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public void setFilesOnlyInB(Collection<String> filesOnlyInB) {
        this.filesOnlyInB = new HashSet<>(filesOnlyInB);
    }

    public List<String> getFilesOnlyInB() {
        return filesOnlyInB.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public void setIntersection(Collection<String> intersection) {
        this.intersection = new HashSet<>(intersection);
    }

    public List<String> getIntersection() {
        return intersection.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public void setModifiedFiles(Collection<String> modifiedFiles) {
        this.modifiedFiles = new HashSet<>(modifiedFiles);
    }

    public List<String> getModifiedFiles() {
        return modifiedFiles.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return serialize();
    }

    public String serialize() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            return jsonResult;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static DirectoriesDifferences deserialize(File json) {
        return deserialize(json.toPath());
    }
    public static DirectoriesDifferences deserialize(Path jsonFile) {
        try {
            String json = String.join("\n", Files.readAllLines(jsonFile));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, DirectoriesDifferences.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

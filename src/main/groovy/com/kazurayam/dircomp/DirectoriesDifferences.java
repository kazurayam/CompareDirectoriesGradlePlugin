package com.kazurayam.dircomp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DirectoriesDifferences {

    /**
     * The files found only in the first directory (A)
     */
    private final Set<Path> filesOnlyInA;

    /**
     * The files found only in the second directory (B)
     */
    private final Set<Path> filesOnlyInB;

    private final Set<Path> intersection;

    /**
     * The files existing in both directories but have different content
     */
    private final Set<Path> modifiedFiles;

    public DirectoriesDifferences(Set<Path> filesOnlyInA,
                                  Set<Path> filesOnlyInB,
                                  Set<Path> intersection,
                                  Set<Path> modifiedFiles) {
        this.filesOnlyInA = filesOnlyInA;
        this.filesOnlyInB = filesOnlyInB;
        this.intersection = intersection;
        this.modifiedFiles = modifiedFiles;
    }

    public Set<Path> getFilesOnlyInA() {
        return filesOnlyInA;
    }

    public String getFilesOnlyInAAsString() {
        return convertSetToJson(filesOnlyInA, "filesOnlyInA");
    }

    public Set<Path> getFilesOnlyInB() {
        return filesOnlyInB;
    }

    public String getFilesOnlyInBAsString() {
        return convertSetToJson(filesOnlyInB, "filesOnlyInB");
    }

    public Set<Path> getIntersection() {
        return intersection;
    }

    public String getIntersectionAsString() {
        return convertSetToJson(intersection, "intersection");
    }

    public Set<Path> getModifiedFiles() {
        return modifiedFiles;
    }

    public String getModifiedFilesAsString() {
        return convertSetToJson(modifiedFiles, "modifiedFiles");
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        String sb = "{\"directoriesDifferences\": {" +
                getFilesOnlyInAAsString() +
                "," +
                getFilesOnlyInBAsString() +
                "," +
                getIntersectionAsString() +
                "," +
                getModifiedFilesAsString() +
                "}}";
        return sb;
    }

    private String convertSetToJson(Set<Path> set, String name) {
        List<Path> list = new ArrayList<>();
        list.addAll(set);
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("{\"%s\":[", name));
        String delim = "";
        for (Path p : list) {
            sb.append(delim);
            sb.append("\"");
            sb.append(p.toString());
            sb.append("\"");
            delim = ",";
        }
        sb.append("]}");
        return sb.toString();
    }

}

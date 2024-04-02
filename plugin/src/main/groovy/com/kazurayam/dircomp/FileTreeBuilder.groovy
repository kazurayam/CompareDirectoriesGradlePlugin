package com.kazurayam.dircomp

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors

class FileTreeBuilder extends SimpleFileVisitor<Path> {

    private static final Logger logger = LoggerFactory.getLogger(FileTreeBuilder.class)
    private Path baseDir
    private Set<Path> containedFiles
    private Set<String> subPaths

    FileTreeBuilder(Path baseDir) {
        Objects.requireNonNull(baseDir)
        assert Files.exists(baseDir)
        this.baseDir = baseDir.toAbsolutePath().normalize()
        this.containedFiles = new HashSet<>()
        this.subPaths = new HashSet<>()
    }

    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        containedFiles.add(file)
        //
        Path relative = baseDir.relativize(file.toAbsolutePath().normalize())
        subPaths.add(relative.normalize().toString())
        //
        return FileVisitResult.CONTINUE
    }

    List<Path> getContainedFiles() {
        return containedFiles.stream().sorted().collect(Collectors.toList())
    }

    Set<String> getSubPaths() {
        return subPaths
    }

    static List<Path> containedFiles(Path baseDir) {
        FileTreeBuilder builder = new FileTreeBuilder(baseDir)
        Files.walkFileTree(baseDir, builder)
        return builder.getContainedFiles()
    }

    static Set<String> scan(Path baseDir) {
        FileTreeBuilder builder = new FileTreeBuilder(baseDir)
        Files.walkFileTree(baseDir, builder)
        return builder.getSubPaths()
    }
}
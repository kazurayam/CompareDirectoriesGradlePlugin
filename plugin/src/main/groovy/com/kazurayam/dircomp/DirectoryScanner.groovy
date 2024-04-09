package com.kazurayam.dircomp

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors

class DirectoryScanner {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryScanner.class)

    private Path baseDir
    private MyFileVisitor visitor

    DirectoryScanner(Path baseDir) {
        Objects.requireNonNull(baseDir)
        assert Files.exists(baseDir)
        this.baseDir = baseDir.toAbsolutePath().normalize()
    }

    DirectoryScanner scan() {
        this.visitor = new MyFileVisitor(baseDir)
        Files.walkFileTree(baseDir, visitor)
        return this
    }

    Set<Path> getFiles() {
        List<Path> files = visitor.getFiles()
        return files.stream().sorted().collect(Collectors.toSet())
    }

    Set<String> getSubPaths() {
        return new TreeSet(visitor.getSubPaths())
    }
}

class MyFileVisitor extends SimpleFileVisitor<Path> {
    private Path baseDir
    private List<Path> files
    private Set<String> subPaths

    MyFileVisitor(Path baseDir) {
        this.baseDir = baseDir
        files = new ArrayList<>()
        subPaths = new HashSet<>()
    }
    @Override
    final FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        files.add(file)
        //
        Path relative = baseDir.relativize(file.toAbsolutePath().normalize())
        subPaths.add(relative.normalize().toString())
        //
        return FileVisitResult.CONTINUE
    }

    List<Path> getFiles() {
        return files
    }

    Set<String> getSubPaths() {
        return subPaths
    }
}
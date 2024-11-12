package com.kazurayam.dircomp

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

/**
 * Data object that contains information of the differences between 2 directories
 */
class DirectoriesDifferences {

    private static Logger logger = LoggerFactory.getLogger(DirectoriesDifferences.class)

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

    private List<Charset> charsetsToTry;

    /**
     * com.fasterxml.jackson.databind requires the default constructor without args
     */
    DirectoriesDifferences() {
        this.dirA = null
        this.dirB = null
        this.filesOnlyInA = new HashSet<>()
        this.filesOnlyInB = new HashSet<>()
        this.intersection = new HashSet<>()
        this.modifiedFiles = new HashSet<>()
        this.charsetsToTry = new ArrayList<>();
        this.charsetsToTry.add(StandardCharsets.UTF_8);
    }

    DirectoriesDifferences(
            Path dirA,
            Path dirB,
            Set<String> filesOnlyInA,
            Set<String> filesOnlyInB,
            Set<String> intersection,
            Set<String> modifiedFiles) {
        this();
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

    void setDirA(Path dirA) {
        this.dirA = dirA
    }

    Path getDirB() {
        return dirB
    }

    void setDirB(Path dirB) {
        this.dirB = dirB
    }

    List<String> getFilesOnlyInA() {
        return filesOnlyInA.stream()
                .sorted()
                .collect(Collectors.toList())
    }

    void setFilesOnlyInA(Set<String> filesOnlyInA) {
        this.filesOnlyInA = filesOnlyInA
    }

    List<String> getFilesOnlyInB() {
        return filesOnlyInB.stream()
                .sorted()
                .collect(Collectors.toList())
    }

    void setFilesOnlyInB(Set<String> filesOnlyInB) {
        this.filesOnlyInB = filesOnlyInB
    }

    List<String> getIntersection() {
        return intersection.stream()
                .sorted()
                .collect(Collectors.toList())
    }

    void setIntersection(Set<String> intersection) {
        this.intersection = intersection
    }

    List<String> getModifiedFiles() {
        return modifiedFiles.stream()
                .sorted()
                .collect(Collectors.toList())
    }

    void setModifiedFiles(Set<String> modifiedFiles) {
        this.modifiedFiles = modifiedFiles
    }

    void addCharsetsToTry(List<String> charsetsToTry) {
        if (charsetsToTry.size() == 0) {
            throw new IllegalArgumentException("charsetsToTry must not be empty")
        }
        charsetsToTry.forEach(name -> {
            this.charsetsToTry.add(Charset.forName(name));
        })
    }


    @Override
    String toString() {
        return toJSON()
    }

    String toJSON() {
        try {
            ObjectMapper mapper = new ObjectMapper()
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(DirectoriesDifferences.class,
                    new DirectoriesDifferencesSerializer())
            mapper.registerModule(simpleModule)

            mapper.enable(SerializationFeature.INDENT_OUTPUT)
            DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter()
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
            String jsonResult =
                    mapper.writer(prettyPrinter).writeValueAsString(this)
            return jsonResult
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e)
        }
    }

    void serialize(Path into) {
        List<String> iterable = new ArrayList<>()
        iterable.add(this.toJSON())
        Files.write(into, iterable, StandardCharsets.UTF_8)
    }

    static DirectoriesDifferences deserialize(File json) {
        return deserialize(json.toPath())
    }

    static DirectoriesDifferences deserialize(Path jsonFile) {
        try {
            String json = String.join("\n", Files.readAllLines(jsonFile))
            ObjectMapper mapper = new ObjectMapper()
            return mapper.readValue(json, DirectoriesDifferences.class)
        } catch (IOException e) {
            throw new RuntimeException(e)
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

    List<String> readAllLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>()
        List<Charset> failedCharsets = new ArrayList<>()
        boolean success = false;
        for (int i = 0; !success && i < charsetsToTry.size(); i++) {
            Charset charset = charsetsToTry.get(i);
            try {
                lines = new ArrayList<>(Files.readAllLines(file, charset))
                success = true;
            } catch (IOException e) {
                failedCharsets.add(charset);
            }
        }
        if (!success) {
            String msg = "Failed to read " + file.toString() +
                    " as a Text file. Tried charsets " + failedCharsets +
                    ". The file could be a binary file" +
                    ", otherwise you may want to add Charset to try.";
            lines.add(msg)
            logger.warn(msg)
        }
        return lines;
    }

    void reportNameStatusList(Path outputText) {
        Set<String> allNames = new TreeSet()
        allNames.addAll(this.filesOnlyInA)
        allNames.addAll(this.filesOnlyInB)
        allNames.addAll(this.intersection)
        allNames.addAll(this.modifiedFiles)
        //
        Files.createDirectories(outputText.getParent())
        OutputStream os = outputText.newOutputStream()
        PrintWriter pw = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(os,
                                StandardCharsets.UTF_8)))
        pw.println("\tname\tstatus\ttimestampA\t<>\ttimestampB\tsizeA\t<>\tsizeB")
        Path ancestorDir = ancestorDirectoryOf(dirA, dirB)
        pw.println("dirA\t${ancestorDir.getFileName().resolve(ancestorDir.relativize(dirA))}")
        pw.println("dirB\t${ancestorDir.getFileName().resolve(ancestorDir.relativize(dirB))}")
        for (String name : allNames) {
            String line= compileNameStatus(name, this.dirA, this.dirB)
            pw.println(line);
        }
        pw.flush()
        pw.close()
    }

    static String compileNameStatus(String name, Path dirA, Path dirB) {
        Path fileA = dirA.resolve(name)
        Path fileB = dirB.resolve(name)
        StringBuilder sb = new StringBuilder()
        sb.append("\t")
        sb.append(name)
        sb.append("\t")
        sb.append(formatFileStatus(fileA, fileB))
        sb.append("\t")
        sb.append(formatFileSize(fileA))
        sb.append("\t")
        sb.append(fileSizeComparison(fileA, fileB))
        sb.append("\t")
        sb.append(formatFileSize(fileB))
        sb.append("\t")
        sb.append(formatLastModified(fileA))
        sb.append("\t")
        sb.append(lastModifiedComparison(fileA, fileB))
        sb.append("\t")
        sb.append(formatLastModified(fileB))
        return sb.toString()
    }

    static String formatFileStatus(Path fileA, Path fileB) {
        if (Files.exists(fileA) && Files.exists(fileB)) {
            if (fileA.size() != fileB.size()) {
                return "M"   // Modified
            } else {
                return "-"    // not modified
            }
        } else if (Files.exists(fileA)) {
            return "D"  // Deleted
        } else {
            return "A"  // Added
        }
    }

    static ZonedDateTime convertLastModifiedToZonedDateTime(Path p) {
        if (Files.exists(p)) {
            FileTime fileTime = Files.getLastModifiedTime(p)
            Instant instant = fileTime.toInstant()
            return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        } else {
            return ZonedDateTime.MIN
        }
    }

    static String formatLastModified(Path p) {
        if (Files.exists(p)) {
            ZonedDateTime ldt = convertLastModifiedToZonedDateTime(p).withNano(0)
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ldt)
        } else {
            return "-"
        }
    }

    static String lastModifiedComparison(Path fileA, Path fileB) {
        if (Files.exists(fileA) && Files.exists(fileB)) {
            ZonedDateTime timestampA = convertLastModifiedToZonedDateTime(fileA)
            ZonedDateTime timestampB = convertLastModifiedToZonedDateTime(fileB)
            int compareResult = timestampA.compareTo(timestampB)
            if (compareResult < 0) {
                return "<"
            } else if (compareResult == 0) {
                return "="
            } else {
                return ">"
            }
        } else {
            return "-"
        }
    }

    static String formatFileSize(Path file) {
        if (Files.exists(file)) {
            return "${file.toFile().size()}"
        } else {
            "-"
        }
    }

    static String fileSizeComparison(Path fileA, Path fileB) {
        if (Files.exists(fileA) && Files.exists(fileB)) {
            long sizeA = fileA.size()
            long sizeB = fileB.size()
            if (sizeA < sizeB) {
                return "<"
            } else if (sizeA == sizeB) {
                return "="
            } else {
                return ">"
            }
        } else {
            return "-"
        }
    }

    /**
     *
     * @param dirA e.g, "/User/foo/bar/buz"
     * @param dirB e.g, "/User/foo/poo/zoo"
     * @return then return "/User/foo" as the common ancestor directory
     */
    static Path ancestorDirectoryOf(Path dirA, Path dirB) {
        Path ancestor = dirA.getRoot()
        for (int i = 0; i < dirA.getNameCount(); i++) {
            if (i < dirB.getNameCount()) {
                if (dirA.getName(i) == dirB.getName(i)) {
                    ancestor = ancestor.resolve(dirA.getName(i))
                } else {
                    return ancestor
                }
            } else {
                return ancestor
            }
        }
        return ancestor
    }
}

# Directories Comparator Gradle Plugin

This project provides a Gradle Plugin, with which you can compare 2 directories to find the differences.

Provided you pass 2 directory A and B to the plugin, it reports the following information:

1. a list of files present in A only which are missing in B.
2. and another list of files missing in A which are present B only.
3. a list of files present in A and B.

The plugin compares the relative path information of files under the base directory; it ignores the other attributes of files, sucha s size, lastModified timestamp, etc.

## how to use

You want to write your build.gradle file as follows:

```
plugin {
    id "com.kazurayam.directoriesComparator" version "0.1.0"
}
...
compareDirectories {
    sourceDir = "src/test/fixtures/A"
    targetDir = "src/test/fixtures/B"
}
```

Then you want to execute:
```
$ ./gradlew compareDirectories
```

You will see the result in the console like this:

```
remainder in source:
    ../src/test/fixtures/A/sub/f.txt
    ../src/test/fixtures/A/sub/i.txt

intersection:
    <baseDir>/d.txt
    <baseDir>/e.txt
    <baseDir>/sub/g.txt

remainder in target:
    ../src/test/fixtures/B/j.txt
    ../src/test/fixtures/B/sub/h.txt


```
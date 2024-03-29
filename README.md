# Compare Directories Gradle Plugin

## Published at the Gradle Plugin Portal

This plugin is published at 

- https://plugins.gradle.org/plugin/com.kazurayam.compareDirectories

## What it is

This project provides a Gradle Plugin, with which you can compare 2 directories to find the differences.

Provided you pass 2 directory A and B to the plugin, it reports the following information:

1. a list of files present in A only which are missing in B.
2. and another list of files missing in A which are present B only.
3. a list of files present in A and B.

The plugin compares the relative path information of files under the base directory; it ignores the other attributes of files, sucha s size, lastModified timestamp, etc.

## how to use

You want to write your build.gradle file as follows:

```
plugins {
    id "com.kazurayam.compare-directories"
}

ext {
    fixturesDir = "../plugin/src/test/fixtures"
    outDir = "build/out"
}

// The com.kazurayam.compare-directories plugin registeres a task named 'compareDirectories'
compareDirectories {
    dirA = layout.projectDirectory.dir("${fixturesDir}/A")
    dirB = layout.projectDirectory.dir("${fixturesDir}/B")
    outputFile = layout.buildDirectory.file("${outDir}/differences.json")
    diffDir = layout.buildDirectory.dir("${outDir}/diff")
}

// or you can register a task with name you like, which calls the CompareDirectoriesTask
tasks.register('dircomp', com.kazurayam.dircomp.CompareDirectoriesTask) {
    dirA = layout.projectDirectory.dir("${fixturesDir}/A")
    dirB = layout.projectDirectory.dir("${fixturesDir}/B")
    outputFile = layout.buildDirectory.file("${outDir}/differences.json")
    diffDir = layout.buildDirectory.dir("${outDir}/diff")
}
```

Then you want to execute:
```
$ gradle dircomp
```

You will see the result in the console like this:

```
> Task :compareDirectories
filesOnlyInA: 1 files
filesOnlyInB: 2 files
intersection: 5 files
modifiedFiles: 1 files
```

## Outputs

The `dircomp` task will create an output tree `build/difference.json`, which contains lines like this:

[![output tree](http://kazurayam.github.io/CompareDirectoriesGradlePlugin/images/output-tree.png)
]()

The `difference.json` file contains a tree of file names categorized as "filesOnlyInA", "filesOnlyInB","intersection" and "modifiedFiles".

```
{
    "dirA": "file:///Users/kazurayam/github/CompareDirectoriesGradlePlugin/plugin-project/plugin/src/test/fixtures/A/",
    "dirB": "file:///Users/kazurayam/github/CompareDirectoriesGradlePlugin/plugin-project/plugin/src/test/fixtures/B/",
    "filesOnlyInA": [
        "sub/i.txt"
    ],
    "filesOnlyInB": [
        "j.txt",
        "sub/h.txt"
    ],
    "intersection": [
        "apple.png",
        "d.txt",
        "e.txt",
        "sub/f.txt",
        "sub/g.txt"
    ],
    "modifiedFiles": [
        "sub/g.txt"
    ]
}
```

The `dircomp` task creates the `diff` directory. In the directory you will find the **unified diff** of each individual "modified" files.

```
--- /Users/kazuakiurayama/github/CompareDirectoriesGradlePlugin/plugin-project/plugin/src/test/fixtures/A/sub/g.txt
+++ /Users/kazuakiurayama/github/CompareDirectoriesGradlePlugin/plugin-project/plugin/src/test/fixtures/B/sub/g.txt
@@ -1,1 +1,1 @@
-g
+g is changed
```


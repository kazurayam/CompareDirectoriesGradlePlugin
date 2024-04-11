# Compare Directories Gradle Plugin

## Published at the Gradle Plugin Portal

This plugin is published at 

- https://plugins.gradle.org/plugin/com.kazurayam.compare-directories

>Previously I named this project as "compareDirectories" and published it at https://plugins.gradle.org/plugin/com.kazurayam.compareDirectories I have renamed it so the previous version will be no longer maintained.

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
    id("com.kazurayam.compare-directories") version "0.2.3"
}

compareDirectories {
    dirA = fileTree(layout.projectDirectory.dir("data/A")) { exclude "**/*.png" }
    dirB = fileTree(layout.projectDirectory.dir("data/B")) { exclude "**/*.png" }
    outputFile = layout.buildDirectory.file("out/differences.json")
    diffDir = layout.buildDirectory.dir("out/diff")
}

tasks.register("dircomp", com.kazurayam.dircomp.CompareDirectoriesTask) {
    dirA = fileTree(layout.projectDirectory.dir("src/test/fixtures/A")) { exclude "**/*.png" }
    dirB = fileTree(layout.projectDirectory.dir("src/test/fixtures/B")) { exclude "**/*.png" }
    outputFile = layout.buildDirectory.file("out/differences.json")
    diffDir = layout.buildDirectory.dir("out/diff")
    doFirst {
        delete layout.buildDirectory.dir("out")
    }
    doLast {
        println "output at " + layout.buildDirectory.dir("out").get()
    }
}

```

Then you want to execute:
```
$ gradle dircomp
```

You will see the result in the console like this:

```
> Task :dircomp
filesOnlyInA: 1 files
filesOnlyInB: 2 files
intersection: 5 files
modifiedFiles: 1 files
output at ...
```

## Outputs

The `dircomp` task will create an output tree `build/out/difference.json`, which contains lines like this:

[![output tree](http://kazurayam.github.io/CompareDirectoriesGradlePlugin/images/output-tree.png)
]()

The `difference.json` file contains a tree of file names categorized as "filesOnlyInA", "filesOnlyInB","intersection" and "modifiedFiles".

```
{
  "dirA" : "file:///Users/kazuakiurayama/github/CompareDirectoriesGradlePlugin/plugin/test-output/com.kazurayam.dircomp.CompareDirectoriesPluginFunctionalTest/data/A/",
  "dirB" : "file:///Users/kazuakiurayama/github/CompareDirectoriesGradlePlugin/plugin/test-output/com.kazurayam.dircomp.CompareDirectoriesPluginFunctionalTest/data/B/",
  "filesOnlyInA" : [ "sub/i.txt" ],
  "filesOnlyInB" : [ "j.txt", "sub/h.txt" ],
  "intersection" : [ "d.txt", "e.txt", "sub/f.txt", "sub/g.txt", "このファイルはシフトJISだよん.txt", "取扱説明書.md" ],
  "modifiedFiles" : [ "sub/g.txt", "このファイルはシフトJISだよん.txt", "取扱説明書.md" ]
}
```

The `dircomp` task creates the `build/out/diff` directory. In the directory you will find the **unified diff** of each individual "modified" files.

```
--- /Users/kazuakiurayama/github/CompareDirectoriesGradlePlugin/plugin/test-output/com.kazurayam.dircomp.CompareDirectoriesPluginFunctionalTest/data/A/sub/g.txt
+++ /Users/kazuakiurayama/github/CompareDirectoriesGradlePlugin/plugin/test-output/com.kazurayam.dircomp.CompareDirectoriesPluginFunctionalTest/data/B/sub/g.txt
@@ -1,1 +1,1 @@
-g
+g is changed
```


Please note that this plugin assumes that all text files are encoded with UTF-8. If any files are encoded with other charsets (such as ShiftJIS), then this plugin would just skip presuming the correct charset; hence the unified-diff will be useless as follows:

`diff/A_B/sub/このファイルはシフトJISだよん.txt`
```
--- /Users/.../data/A/このファイルはシフトJISだよん.txt
+++ /Users/.../data/B/このファイルはシフトJISだよん.txt
@@ -1,1 +1,1 @@
-Failed to read /Users/.../data/A/このファイルはシフトJISだよん.txt as a text in UTF-8
+Failed to read /Users/.../data/B/このファイルはシフトJISだよん.txt as a text in UTF-8
```
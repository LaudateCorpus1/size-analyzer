# Size Analyzer

## IMPORTANT: Experimental project

The Size Analyzer was an experimental project that is not actively supported by
Google. Developers are free to fork and improve it under the terms of the Apache
license.

## Description

The Size Analyzer is a tool for developers to understand the size of their
Android application.

## How to build the size analyzer
The tool can be built using [gradle](https://gradle.org/). An executable jar can
be built using the command below:

``` shell
./gradlew :analyzer:executableJar
```

## How to use the size analyzer

The executable jar can be run against either an Android Studio project or an
[Android App Bundle](https://g.co/androidappbundle).

```shell
java -jar analyzer/build/libs/analyzer.jar check-bundle <path-to-aab>
java -jar analyzer/build/libs/analyzer.jar check-project <path-to-project-directory>
```

## Binary distributions
Pre-built distributions of this tool will be made available with each release
on our [releases page](https://github.com/android/size-analyzer/releases).

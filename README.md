# jca-test-suite

## Overview
A project for testing the correctness and performance of the EPICS CA software for Java.  In particular the [epics2web](https://github.com/JeffersonLab/epics2web) service is tested against clients configured to directly query a gateway via either [CAJ](https://github.com/epics-base/caj) or the newer Java 8 [CA](https://github.com/channelaccess/ca) library.

## How to build

On Windows run:

``
gradlew.bat build
``

On UNIX run:

``
gradlew build
``

The build script does everything including downloading all dependencies from the Internet.

## How to run tests
Assumes UNIX.  Append ".bat" to gradlew if on Windows.

1. First run CAServer

``
gradlew run
``

2. Next run one of the clients

3. Stop the CAServer

``
gradlew stop
``

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

The server creates 5,000 integer counter PVs which update at 100 times a second.  The PVs are named counter0, counter1, etc.

``
gradlew run
``

2. Next run one of the clients

``
gradlew client -Pclientargs=caj,hello
``

Where "caj" can be substituted for one of the clients and "hello" can be substituted for one of the tests

### Clients
| Client | Description |
|--------|-------------|
| caj    | JCA/CAJ     |
| j8     | Java 8 CA   |
| ws     | Web Socket  |

### Tests
| Test        | Description |
|-------------|-------------|
| hello       | Connects to counter0 PV for a few seconds |
| throughput  | Connects to 5,000 unique counter PVs for 30 seconds |
| concurrency | Launches 100 internal clients, which then monitor 100 unique PVs each for 30 seconds |
| slow        | Similar to throughput test, but update callback puts thread to sleep for 10 seconds to slow things down; additionally ws client application recv-q buffer size is set to 512 bytes from default of 4MB so that backpressure is created much more quickly for ws client (caj and j8 don't have an application recv-q buffer, just OS recv-q) |

Note: the "ws" client requires epics2web to be running.

### Configuration
#### WSClient
You can configure the URL to epics2web by copying the config/wsclient-default.properties to a new file named wsclient.properties and modifying the epics2web_monitor_url property.  The default is localhost port 8080 at path /epics2web/monitor.  The new properties file must be in the classpath, such as by keeping it in the config directory.
#### J8Client
You can create a file named j8client.properties and include it in the config directory to control configuration of the J8Client.  the options are described [here](https://github.com/channelaccess/ca).   The defaults set in config/j8client-default.properties are to look for the CAServer on localhost only.

3. Stop the CAServer

``
gradlew stop
``

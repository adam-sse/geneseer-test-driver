# geneseer-test-driver

This is a utility program that is used by [geneseer](https://github.com/adam-sse/geneseer). Its purpose is to execute
Junit 4 and 3.8.x tests and print the results via Java serialization to stdout.

## Running

Start a JVM with the classpath set up to execute the tests. This should include the classes of the system under test,
the classes containing the test cases, and any libraries required. If the `jar-with-dependencies` of this test driver is
used, Junit does not need to be included in the classpath as it is already included in the fat jar. Otherwise, include
Junit 4 in the classpath (even if you want to execute Junit 3.8.x tests).

The main class is `net.ssehub.program_repair.geneseer.evaluation.TestDriver`.

Here are example invocations (using the Unix file separator character `:`):
```
java -cp geneseer-test-driver-jar-with-dependencies.jar:path/to/sut/classes/:path/to/test/classes/:path/to/lib.jar net.ssehub.program_repair.geneseer.evaluation.TestDriver
```

This program is compiled with Java 7, so that it works on that and any later versions.

## Input & Output

This program reads Java serialized commands from stdin. Commands are a single `java.lang.String`, with further arguments
following. The results are written via Java serialization to stdout. The normal stdout and stderr output of the test
execution are suppressed.

There are three commands available:

* `"CLASS"`: another `java.lang.String` after this command specifies the fully qualified class name of a test class to
run. The are loaded via
[`Class.forName()`](https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#forName%28java.lang.String%29). If
loading a class fails, no tests are executed. The result of this command are the executed tests as
`java.util.List<net.ssehub.program_repair.geneseer.evaluation.TestResult>`

* `"METHOD"`: two `java.lang.String` after this command specify the fully qualified class name of a test class and the
name of a test method within that class (no parenthesis or arguments). The result of this command is either a
`net.ssehub.program_repair.geneseer.evaluation.TestResult` of the executed test, or `null` if either the class could
not be loaded (see above) or the test method does not exist within that class.

* `"HEARTBEAT"`: The result of this command is a single `java.lang.String` with the content `"alive"`. This is useful
for checking if the process is still responding (e.g. during debugging).

Note that you do not need to depend on this project to deserialize the `TestResult` class. It is possible to create a
structurally equivalent class and deserialize into that. This requires:

* A class or record called `TestResult` in the package `net.ssehub.program_repair.geneseer.evaluation`
* The serial version identifier `private static final long serialVersionUID = 5281136086896771809L`
* The following attributes, all of type `java.lang.String`
    * `testClass`
    * `testMethod`
    * `failureMessage`
    * `failureStacktrace`

## Debug Output

The test driver can print log debug output to stderr. To enable this, pass `debug` (case insensitive) as the first
command line argument (i.e. after the fully qualified class name of the main class
`net.ssehub.program_repair.geneseer.evaluation.TestDriver`). The test driver will then print what it's currently doing
to stderr. This is meant for human consumption and  should not be parsed. Additionally, the stdout and stderr of the
test cases being run in printed to stderr.

## Compiling

This project uses [Maven](https://maven.apache.org/) for dependency management and the build process. To simply build
jars, run:
```
mvn package
```

This creates two jar files in the `target` folder (`$version` is the version that was built, e.g. `2.0.0`
or `1.2.1-SNAPSHOT`):

* `geneseer-test-driver-$version.jar` just includes the class files of this program.
* `geneseer-test-driver-$version-jar-with-dependencies.jar` includes the class files of this program, plus all
dependencies. This means that this jar can be used when you don't want to manually provide all dependencies of this
program each time you execute it.

When other projects require this project as a dependency in Maven, you need to install it to the local Maven repository.
They usually require a specific version, so you need to check that out first (using `2.0.0` in this example). Run:
```
git checkout v2.0.0
mvn install
``` 

# geneseer-test-driver

This is a utility program that is used by [geneseer](https://github.com/adam-sse/geneseer). Its purpose is to execute
Junit 4 and 3.8.x tests and print the results via Java serialization to stdout.

## Running

Start a JVM with the classpath set up to execute the tests. This should include the classes of the system under test,
the classes containing the test cases, and any libraries required. If the `jar-with-dependencies` of this test driver is
used, Junit does not need to be included in the classpath as it is already included in the fat jar. Otherwise, include
Junit 4 in the classpath (even if you want to execute Junit 3.8.x tests).

There are two main classes, one for running a whole suite of test classes, and one for running only a single test
method (useful when collecting coverage for a single test method):

* `net.ssehub.program_repair.geneseer.evaluation.ClassListRunner` runs a set of test classes. The command line arguments
are fully qualified class names of the the test classes to run. They are loaded via
[`Class.forName()`](https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#forName%28java.lang.String%29). If
loading a class fails, a stacktrace is written to the error output string (see below) and execution continues with the
next class.
* `net.ssehub.program_repair.geneseer.evaluation.SingleTestMethodRunner` runs a single test method. The first command
line argument is the fully qualified class name of the test class. It is loaded via
[`Class.forName()`](https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#forName%28java.lang.String%29). If
loading a class fails, a stacktrace is written to the error output string (see below) and no tests are executed. The
second command line argument is the name of the test method (no parenthesis or arguments). 

Here are example invocations (using the Unix file separator character `:`). To run all tests in two given classes:
```
java -cp geneseer-test-driver-jar-with-dependencies.jar:path/to/sut/classes/:path/to/test/classes/:path/to/lib.jar net.ssehub.program_repair.geneseer.evaluation.ClassListRunner some.TestClass other.TestClass
```
To run only a single test method:
```
java -cp geneseer-test-driver-jar-with-dependencies.jar:path/to/sut/classes/:path/to/test/classes/:path/to/lib.jar net.ssehub.program_repair.geneseer.evaluation.SingleTestMethodRunner some.TestClass methodName
```

This program is compiled with Java 7, so that it works on that and any later versions.

## Output

This program prints its result via Java serialization to stdout. The normal stdout and stderr output of the test
execution is captured and saved in string format; it is not sent to the normal stdout/stderr when running this test
driver. The serialized output is:

1. The captured stdout as `java.lang.String`
2. The captured stderr as `java.lang.String`
3. The executed tests as `java.util.List<net.ssehub.program_repair.geneseer.evaluation.TestResult>`

Note that you do not need to depend on this project to deserialize the `TestResult` class. It is possible to create a
structurally equivalent class and deserialize into that. This requires:

* A class or record called `TestResult` in the package `net.ssehub.program_repair.geneseer.evaluation`
* The serial version identifier `private static final long serialVersionUID = 5281136086896771809L`
* The following attributes, all of type `java.lang.String`
    * `testClass`
    * `testMethod`
    * `failureMessage`
    * `failureStacktrace`

For debugging purposes, you can pass the system property `nowrap` to the JVM (i.e. `-Dnowrap` added to the JVM
arguments, *not* the command line arguments of this program). This will cause this test driver to not capture the stdout
and stderr output of the test execution, so that it is printed normally. Also, the test results are not written in
serialized form to stdout; instead, failed tests are written in human-readable text form to stdout after all tests have
finished.

## Compiling

This project uses [Maven](https://maven.apache.org/) for dependency management and the build process. To simply build
jars, run:
```
mvn package
```

This creates two jar files in the `target` folder (`$version` is the version that was built, e.g. `1.0.0`
or `1.0.1-SNAPSHOT`):

* `geneseer-test-driver-$version.jar` just includes the class files of this program.
* `geneseer-test-driver-$version-jar-with-dependencies.jar` includes the class files of this program, plus all
dependencies. This means that this jar can be used when you don't want to manually provide all dependencies of this
program each time you execute it.

When other projects require this project as a dependency in Maven, you need to install it to the local Maven repository.
They usually require a specific version, so you need to check that out first (using `1.0.0` in this example). Run:
```
git checkout v1.0.0
mvn install
``` 

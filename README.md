# geneseer-test-driver

This is a utility program that is used by [geneseer](https://github.com/adam-sse/geneseer). Its purpose is to execute
Junit 4 and 3.8.x tests and print the results via Java serialization to stdout.

## Running

Start a JVM with the classpath set up to execute the tests. This should include the classes of the system under test,
the classes containing the test cases, and any libraries required. If the `jar-with-dependencies` of this test driver is
used, Junit does not need to be included in the classpath as it is already included in the fat jar. Otherwise, include
Junit 4 in the classpath (even if you want to execute Junit 3.8.x tests).

The main class of this test driver is `net.ssehub.program_repair.geneseer.evaluation.JunitRunnerClient`. The command
line arguments are fully qualified class names of the test classes. They are loaded via
[`Class.forName()`](https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#forName%28java.lang.String%29). If
loading a class fails, a stacktrace is written to the error output string (see below) and execution continues with the
next class.

An example invocation may look like this (using the Unix file separator character `:`):
```
java -cp geneseer-test-driver-jar-with-dependencies.jar:path/to/sut/classes/:path/to/test/classes/:path/to/lib.jar net.ssehub.program_repair.geneseer.evaluation.JunitRunnerClient some.TestClass other.TestClass
```

This program is compiled with Java 7, so that it works on that and any later versions.

## Output

This program prints its result via Java serialization to stdout. The normal stdout and stderr output of the test
execution is captured and saved in string format; it is not sent to the normal stdout/stderr when running this test
driver. The serialized output is:

1. The captured stdout as `java.lang.String`
2. The captured stderr as `java.lang.String`
3. The failed tests as `java.util.List<net.ssehub.program_repair.geneseer.evaluation.TestFailure>`

Note that you do not need to depend on this project to deserialize the `TestFailure` class. It is possible to create a
structurally equivalent class and deserialize into that. This requires:

* A class or record called `TestFailure` in the package `net.ssehub.program_repair.geneseer.evaluation`
* The serial version identifier `private static final long serialVersionUID = 349718598568296233L`
* The following attributes, all of type `java.lang.String`
    * `testClass`
    * `testMethod`
    * `message`
    * `stacktrace`

For debugging purposes, you can pass the system property `nowrap` to the JVM (i.e. `-Dnowrap` added to the JVM
arguments, *not* the command line arguments of this program). This will cause this test driver to not capture the stdout
and stderr output of the test execution, so that it is printed normally. Also, the test failures are not written in
serialized form to stdout; instead they are written in human-readable text form to stdout after all tests have finished.

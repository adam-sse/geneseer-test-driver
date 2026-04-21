# geneseer-test-driver

This is a utility program that is used by [geneseer](https://github.com/adam-sse/geneseer). Its purpose is to execute
JUnit 4 and 3.8.x tests and print the results via Java serialization to stdout.

## Running

Start a JVM with the classpath set up to execute the tests. This should include the classes of the system under test,
the classes containing the test cases, and any libraries required. If the `jar-with-dependencies` of this test driver is
used, JUnit does not need to be included in the classpath as it is already included in the fat jar. Otherwise, include
JUnit 4 in the classpath (even if you want to execute JUnit 3.8.x tests).

The main class is `net.ssehub.program_repair.geneseer.evaluation.TestDriver`.

Here are example invocations (using the Unix file separator character `:`):
```
java -cp geneseer-test-driver-jar-with-dependencies.jar:path/to/sut/classes/:path/to/test/classes/:path/to/lib.jar net.ssehub.program_repair.geneseer.evaluation.TestDriver
```

This program is compiled with Java 8, so that it works on that and any later versions.

## Input & Output

This program reads Java serialized commands from stdin. Commands are a single `java.lang.String`, with further arguments
following. The results are written via Java serialization to stdout. The normal stdout and stderr output of the test
execution are suppressed.

There are three commands available:

* `"CLASS"`: another `java.lang.String` after this command specifies the fully qualified class name of a test class to
run. The result of this command are the executed tests as
`java.util.List<net.ssehub.program_repair.geneseer.evaluation.TestResult>`

* `"METHODS"`: same as `"CLASS"`, but after each test method, three `java.lang.String` are output:
    1. the constant `"TEST_FINISHED"`
    2. the name of the test class (as in a TestResult)
    3. the name of the test method (as in a TestResult)
The test driver then waits until a single `java.lang.String` with the value `"CONTINUE"` is supplied. The intention here
is that coverage data for each individual test method can be collected. Finally, the constant `java.lang.String`
`"DONE"` is output, before the list of `TestsResult`s as in the `"CLASS"` command above.

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

## Test class loading

Test classes are loaded using a mechanism behaving similar to
[`Class.forName()`](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#forName-java.lang.String-). This
means that the classpath of this test driver process needs to include the test classes as well as any dependencies they
require. If loading a class fails, the test driver process prints an exception to stderr and terminates.

Some test suites benefit from isolating test classes via separate class loaders. This prevents leftover static
initialization from carrying over between test classes. However, in some cases (e.g. with JDBC drivers) this causes
problems because they rely on static initialization that must persist across the entire JVM.

By default, a separate class loader is used per test class. This class loader still uses the full classpath of this test
driver process, so discovers the same classes as a normal `Class.forName()` call. To disable this default behavior, pass
`--no-per-test-classloader` as a command line argument.

## Debug Output

The test driver can print log debug output to stderr. To enable this, pass `--debug` as a command line argument. The
test driver will then print what it's currently doing to stderr. This is meant for human consumption and should not be
parsed. Additionally, the stdout and stderr of the test cases being run are printed to stderr.

The debug messages contain a timestamp in the system timezone by default. As the system timezone may be overridden (e.g.
for test suite stability), a different timezone for debug messages can be specified using `geneseer.logTimeZone` system
property (for example, via `-Dgeneseer.logTimeZone=Europe/Berlin`).

## Compiling

This project uses [Maven](https://maven.apache.org/) for dependency management and the build process. To simply build
jars, run:
```
mvn package
```

This creates two jar files in the `target` folder (`$version` is the version that was built, e.g. `3.0.0`
or `3.1.0-SNAPSHOT`):

* `geneseer-test-driver-$version.jar` just includes the class files of this program.
* `geneseer-test-driver-$version-jar-with-dependencies.jar` includes the class files of this program, plus all
dependencies. This means that this jar can be used when you don't want to manually provide all dependencies of this
program each time you execute it.

When other projects require this project as a dependency in Maven, you need to install it to the local Maven repository.
They usually require a specific version, so you need to check that out first (using `3.0.0` in this example). Run:
```
git checkout v3.0.0
mvn install
``` 

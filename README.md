# vehicleLang

vehicleLang is a probabilistic modeling and simulation language for vehicular cyber attacks. More specifically, it is a domain specific language (DSL) created with [MAL (the Meta Attack Language)](https://github.com/pontusj101/MAL). All the work on vehicleLang was done in the context of my Master Thesis at KTH Royal Institute of Technology, Stockholm, Sweden which was funded by the Threat MOVE project.

## Getting Started

These instructions will guide you on how to have a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

For this project to work, the MAL compiler, found on the [MAL project](https://github.com/pontusj101/MAL) is needed.

### Project's file structure

The language itself is found on the *.mal files of this project while the test cases are located on the [src/test/java](src/test/java) folder.
The compiler generated vehicleLang.html file (typically found under the ./target/generated-sources directory of the project) provides a useful representation of all the modeled attack steps on every modeled asset.
Finally, on the [doc](doc/) folder the documentation of vehicleLang can be found.

### Build for MAL stand-alone

Since this is a Maven project it is ought to be opened by any compatible IDE or to be used with the mvn command line tool.

To build the project and run the test cases simply issue the following command on the command line:

```
mvn clean install
```

If you want to just built the project without running the test cases do the following:

```
mvn clean install -DskipTests
```

You can also simply compile the language using the mal-compiler.jar as shown on the readme of the MAL compiler repo.

Once built, the resulting language JAR can be used to create models and test cases in Java (see `src/test/java/` for examples).

### Build for use with securiCAD

Building a vehicleLang JAR for use in securiCAD requires some additional prerequisites and a slightly different build procedure.
The MAL compiler contains [instructions](https://github.com/pontusj101/MAL/blob/master/src/main/java/com/foreseeti/generator/README.md) on prerequisites for building a language JAR for securiCAD.
In short, the following has to be done:
* Make sure that securiCAD compile time dependencies `corelib` and `simulator` are available in the local Maven repo. See [Installing corelib and simulator](#installing-corelib-and-simulator) below. 
* Make sure the language only uses securiCAD-supported categories (from the set 'Communication', 'Container', 'Networking', 'Security', 'System', 'User', 'Zone').
* Optionally provide a directory containing the asset icons and uncomment the line in `foreseetipom.xml` containing the `-v` switch and update the path to the directory

Once the prerequisites are met, the securiCAD-compatible vehicleLang is built by running:
```
mvn -f foreseetipom.xml package -Dmaven.test.skip
```

If successful, the JAR can be picked up from the `target` directory as `target/vehicleLang-<ver>.jar`.

Provide the switch `-lang=<full-path-to-jar>` to the securiCAD Professional executable to run it with the language JAR.

Note: Compiling for securiCAD required Java 11 to be used. This is preconfigured in the `foreseetipom.xml` file.
Note 2: `-Dmaven.test.skip` is required for the Maven Surefire plugin to skip compiling the awsLang tests which require the standard generator backend to be used.  

#### Installing corelib and simulator

The foreseeti kernel-CAD JARs `corelib` and `simulator` are automatically placed in the local Maven repository if you build the project.

If you cannot build kernel-CAD from source, you can obtain the JAR files and install them manually by running the following command:

```
mvn install:install-file -Dfile=./corelib-1.5.0.jar -DgroupId=com.foreseeti -DartifactId=corelib -Dversion=1.5.0 -Dpackaging=jar 
mvn install:install-file -Dfile=./simulator-1.5.0.jar -DgroupId=com.foreseeti -DartifactId=simulator -Dversion=1.5.0 -Dpackaging=jar
```

Where `corelib-1.5.0.jar` is the JAR file and `1.5.0` is the version you wish to assign to it in the Maven repo. 
Normally, you wish to use the version of the JAR given but you can make up your own version. If the version number in the above example had been different from the file name, the command would have renamed the file according to the specified version when placing it in the Maven repo.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Sotirios Katsikeas**

## Contributors

* **Nedo Skobalj**

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details

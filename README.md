# vehicleLang

vehicleLang is a probabilistic modeling and simulation language for vehicular cyber attacks. More specifically, it is a domain specific language (DSL) created with [MAL (the Meta Attack Language)](https://github.com/pontusj101/MAL). All the work on vehicleLang was done in the context of Master Thesis at KTH Royal Institute of Technology, Stockholm, Sweden and was funded by the Threat MOVE project.

## Getting Started

These instructions will guide you on how to have a copy of the project up and running on your local machine for development and testing purposes.

### Project's file structure

This project has the following structure:

* The file `pom.xml` is the Maven configuration file of the project.
* The language itself, the MAL specification, is found on the *.mal files located on the [src/main/mal](src/main/mal) directory.
* The unit and integration test cases of vehicleLang are located on the [src/test/java](src/test/java) directory.
* The directory src/main/resources/icons should contain the SVG icons for the assets in vehicleLang.
* Finally, on the [doc](doc/) folder the documentation of vehicleLang can be found.

Since this is a Maven project it is ought to be opened by any compatible IDE or to be used with the mvn command line tool.

### Building a securiCAD compatible .jar file and running the tests

Building a vehicleLang JAR for use in securiCAD does only require access on the proper repository from where all the needed packages will be automatically downloaded during building.

So, the securiCAD-compatible vehicleLang can be built by running:
```
mvn package
```

If successful, the JAR can be picked up from the `target` directory as `target/vehicleLang-<ver>.jar`.

Note: Compiling for securiCAD requires Java 11 to be used.

### Only building a securiCAD compatible .jar file

If you don't want to run the unit tests, you can build a securiCAD compatible `.jar` file with the following command:

```
mvn package -P build-only
```

The resulting `.jar` file will be located in `target/examplelang-1.0.0.jar`.

### Only running the test cases of the language

If you don't want to build a securiCAD compatible .jar file, you can run the unit tests with the following command:

```
mvn test -P test-only
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Sotirios Katsikeas**

## Contributors

* **Nedo Skobalj**

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details

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

### Usage

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

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Sotirios Katsikeas**

## Contributors

* **Nedo Skobalj**

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details

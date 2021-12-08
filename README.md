# scala-fx

## Getting started

scala-fx is a library built using [Project Loom](https://jdk.java.net/loom/). It is intended to bring the power of continuations to Scala.

Continuations are useful because for the first time ever in Scala, we will have a way to a way to handle errors (happy vs bad path) without the need for traditional FP "box-type" encodings such as `map` or `flatMap`.

This means you can encode pure programs without the need for adhering to the style in libraries like cats-effect or ZIO, but you will still have the compile-time guarantees of the traditional box encoding.

### Build and run in your local environment:

Pre-requisites:

- [Project Loom](https://jdk.java.net/loom/)
- Scala 3

1. Download the latest Project Loom [Early-Access build](https://jdk.java.net/loom/) for your system architecture.
2. Set your `JAVA_HOME` to the path you extracted above.

You can now compile and run the tests:

```shell
sbt "clean; compile; test"
```

# kodvent

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/dmitrynekrasov/kodvent/build.yml)](https://github.com/DmitryNekrasov/kodvent/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dmitrynekrasov/kodvent.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.dmitrynekrasov/kodvent)
[![KDoc link](https://img.shields.io/badge/API_reference-KDoc-blue)](https://dmitrynekrasov.github.io/kodvent/)

![kodvent](logo.jpg)

A Kotlin utility library for Advent of Code challenges, providing helpful extension functions for common data structure operations.

## Features

### Map Counter Extensions

The library provides convenient extension functions for `MutableMap<T, Int>` to easily maintain counter/frequency maps:

- **`increment(key: T)`** - Increments the count for a key, initializing to 1 if the key doesn't exist
- **`decrement(key: T): Boolean`** - Decrements the count for a key, removing it when count reaches 0

### Math Functions

Common mathematical functions for number theory and algorithmic problems:

- **`gcd(a: Int, b: Int): Int`** - Computes the greatest common divisor (GCD) of two integers using the Euclidean algorithm
- **`lcm(a: Int, b: Int): Int`** - Computes the least common multiple (LCM) of two integers
- Both functions are also available for `Long` parameters

## Installation

Add the dependency to your project:

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.dmitrynekrasov:kodvent:0.1.3")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.dmitrynekrasov:kodvent:0.1.3'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.dmitrynekrasov</groupId>
    <artifactId>kodvent</artifactId>
    <version>0.1.3</version>
</dependency>
```

## Usage

### Counting Elements

```kotlin
val frequencies = mutableMapOf<Char, Int>()

// Count character occurrences
"hello world".forEach { char ->
    frequencies.increment(char)
}

// frequencies now contains: {h=1, e=1, l=3, o=2, w=1, r=1, d=1}
```

### Mathematical Computations

```kotlin
import kodvent.math.gcd
import kodvent.math.lcm

// Find the greatest common divisor
val result1 = gcd(48, 18)  // 6

// Simplify a fraction using GCD
val numerator = 48
val denominator = 180
val divisor = gcd(numerator, denominator)
val simplified = "${ numerator / divisor }/${ denominator / divisor }"  // "4/15"

// Find the least common multiple
val result2 = lcm(12, 18)  // 36

// Solve a scheduling problem
// Two buses arrive every 12 and 18 minutes
val nextSimultaneousArrival = lcm(12, 18)  // 36 minutes
```

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

The project includes comprehensive tests for all extension functions. Test reports are generated at `build/reports/tests/test/index.html`.

## Requirements

- Kotlin 2.2.20+
- JVM 23+

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

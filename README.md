# kodvent

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)

![kodvent](logo.jpg)

A Kotlin utility library for Advent of Code challenges, providing helpful extension functions for common data structure operations.

## Features

### Map Counter Extensions

The library provides convenient extension functions for `MutableMap<T, Int>` to easily maintain counter/frequency maps:

- **`increment(key: T)`** - Increments the count for a key, initializing to 1 if the key doesn't exist
- **`decrement(key: T): Boolean`** - Decrements the count for a key, removing it when count reaches 0

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

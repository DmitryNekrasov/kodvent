# kodvent

## What it is

- A Kotlin Multiplatform library of helpers for Advent of Code and competitive programming: data
  structures, algorithms and extension functions. It has zero dependencies, is published to Maven
  Central, has one maintainer and is at version 0.x.
- All code is common Kotlin in `src/commonMain`, compiled for JVM (bytecode 1.8), JS, Wasm and Native.
  There is no `expect`/`actual` and no platform source set.
- It is called from single-threaded puzzle solutions, on input of puzzle size that the caller trusts.

## What a real failure looks like

- A function returns a wrong result: an off-by-one at a boundary, an `Int` or `Long` overflow on a
  realistic input.
- The same call behaves differently on JVM, JS, Wasm and Native.
- Code throws, or does not throw, against what its KDoc says.
- A documented complexity is not met.
- KDoc, the README, `Module.md` or a sample states something the code does not do.
- The public API changes by accident, or without the dumps in `api/`.
- A test that cannot fail, or documented behaviour that no test checks.

## Blast radius

- A user gets a wrong puzzle answer or a crash in a solution of their own. There are no services, no
  stored data and no untrusted input behind this library.

## The reporting bar

A finding must matter to such a user. Noise here:

- thread safety, and hardening against hostile input
- limits that need more memory than a machine has, like a collection of 2^31 elements
- Java interoperability
- new dependencies, extra abstraction, configuration options and API nobody asked for
- a missing comment, and style points the Kotlin official code style does not make

## Where the rules live

- `README.md`, the sections Development and Contributing.
- `build.gradle.kts`: the targets and the compiler options.
- `api/kodvent.api` and `api/kodvent.klib.api`: the public API. `./gradlew build` checks the code
  against them, and `./gradlew updateKotlinAbi` regenerates them after an intended change.
- `.idea/codeStyles`: the Kotlin official code style.
- There is no `CLAUDE.md`, `AGENTS.md` or `CONTRIBUTING.md`.

## Conventions that are deliberate

- `explicitApi()` and `allWarningsAsErrors` are on.
- `-Xreturn-value-checker=full` is on: the result of every function must be used, unless the function
  carries `@IgnorableReturnValue`. The annotation is a decision about that function.
- The public API has short KDoc that states the complexity where it matters and points to a
  `@sample`. The samples are `@Test` functions in `src/commonTest/kotlin/samples`: they run as tests,
  and Dokka prints them with `failOnWarning` on.
- `Module.md` is the front page of the Dokka documentation and the description of every package. The
  solutions it prints under "With kodvent" are also tests in `samples/PuzzleSamples.kt`. No `@sample`
  points to that file, and it changes together with `Module.md`. The README also prints the solution
  of Reindeer Maze, with the line counts of the 3 puzzles.
- The code documents itself. A comment stays only for a reason the code cannot show: a measured
  performance decision, an overflow argument, a platform difference, a binary-interface constraint.
- A performance decision is measured, and the comment next to it gives the result. Undoing it needs
  a measurement.
- A KDoc sentence that says what a type deliberately is not, like "A grid is not a collection", is a
  decision, not a gap.
- A function that has nothing to return throws: `IndexOutOfBoundsException` for an index or a position
  outside, `IllegalArgumentException` through `require` for a bad argument, `NoSuchElementException`
  when nothing is there. Where a null result is useful, there is an `OrNull` sibling.
- A factory function may be named like its type, with `@Suppress("FunctionName")`.
- Tests use `kotlin.test` with power-assert, and nothing else.

## Languages

- Kotlin for the library, the tests and the samples, Gradle Kotlin DSL for the build, Markdown for the
  README and `Module.md`. The Kotlin conventions above are not the bar for the build script, the README
  or `Module.md`.

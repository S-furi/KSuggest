# KSuggest

Autocomplete Kotlin-Stdlib definitions.

## Usage

Simply with:
```bash
./solution.sh <definition-prefix>
```
or directly with gradle:
```bash
./gradlew run --quiet --args <definition-prefix>
```

### Experimental
A Kotlin Script is also provided (`solution.kts` file), but given
its beta stage, it is considered as experimental.
An expection is raised at the end of the experiment (`java.lang.IllegalAccessError`),
possibly due to the limited nature of kotlin scripts. Howeever,
correct output is correclty display if the exception is ignored.

Kotlin script is runnable with:
```bash
./solution.kts <definition-prefix>
```

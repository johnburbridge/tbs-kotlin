# Time-Based Storage (TBS)

[![CI](https://github.com/johnburbridge/tbs-kotlin/actions/workflows/ci.yaml/badge.svg)](https://github.com/johnburbridge/tbs-kotlin/actions/workflows/ci.yaml)
[![codecov](https://codecov.io/gh/johnburbridge/tbs-kotlin/branch/main/graph/badge.svg)](https://codecov.io/gh/johnburbridge/tbs-kotlin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Kotlin library for storing and retrieving values based on timestamps. This library provides thread-safe implementations of time-based storage using different underlying data structures.

## Features

- Thread-safe time-based storage implementations
- Support for multiple underlying data structures (HashMap, BTree, RBTree)
- Efficient range queries and duration-based retrievals
- Automatic timestamp collision resolution
- Wait mechanisms for data availability
- Comprehensive test coverage

## Installation

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.johnburbridge:tbs:1.0.0")
}
```

## Usage

### Basic Usage

```kotlin
import com.github.johnburbridge.tbs.concurrent.ThreadSafeTimeBasedStorage
import com.github.johnburbridge.tbs.core.HashMapTimeBasedStorage
import java.time.Instant

// Create a thread-safe storage using HashMap as the underlying implementation
val storage = ThreadSafeTimeBasedStorage(HashMapTimeBasedStorage<String>())

// Add values with timestamps
storage.add(Instant.now(), "value1")
storage.add(Instant.now().minusSeconds(10), "value2")

// Retrieve values
val value = storage.getValueAt(Instant.now())
val recentValues = storage.getDuration(Duration.ofSeconds(30))
```

### Available Implementations

1. **HashMapTimeBasedStorage**: Uses a HashMap for O(1) lookups but O(n) range queries
2. **BTreeTimeBasedStorage**: Uses a B-tree for balanced performance across all operations
3. **RBTreeTimeBasedStorage**: Uses a Red-Black tree for guaranteed balanced performance

### Thread Safety

The library provides thread-safe wrappers for all implementations:

```kotlin
// Thread-safe storage
val threadSafeStorage = ThreadSafeTimeBasedStorage(HashMapTimeBasedStorage<String>())

// Safe for concurrent access
threadSafeStorage.add(Instant.now(), "value")
```

### Advanced Features

#### Range Queries

```kotlin
val startTime = Instant.now().minusSeconds(30)
val endTime = Instant.now()
val valuesInRange = storage.getRange(startTime, endTime)
```

#### Duration-Based Retrieval

```kotlin
// Get all values from the last 5 minutes
val recentValues = storage.getDuration(Duration.ofMinutes(5))
```

#### Timestamp Collision Resolution

```kotlin
// If a timestamp collision occurs, addUniqueTimestamp will find the next available timestamp
val newTimestamp = storage.addUniqueTimestamp(Instant.now(), "value", 1)
```

#### Waiting for Data

```kotlin
// Wait for data with timeout
val hasData = storage.waitForData(1000L) // Wait up to 1 second

// Wait indefinitely
storage.waitForData(null)
```

## Performance Characteristics

| Operation | HashMap | BTree | RBTree |
|-----------|---------|-------|--------|
| Add | O(1) | O(log n) | O(log n) |
| Get | O(1) | O(log n) | O(log n) |
| Range Query | O(n) | O(log n + k) | O(log n + k) |
| Memory | O(n) | O(n) | O(n) |

Where:
- n is the number of entries
- k is the number of entries in the range

## Examples

See the `examples` package for complete examples:

- `RBTreeExample`: Basic usage examples
- `PerformanceBenchmark`: Performance comparison between implementations

## Thread Safety

Thread-safe implementations provide:
- Read-write locks for safe concurrent access
- Condition variables for waiting on data availability
- Notification mechanism when new data is added

Example with waiting:

```kotlin
val storage = TimeBasedStorages.createThreadSafeRBTreeStorage<String>()

// In a consumer thread:
if (storage.isEmpty()) {
    // Wait for up to 5 seconds for data to arrive
    storage.waitForData(5000)
}
val data = storage.getAll()

// In a producer thread:
storage.add(Instant.now(), "New event")
// Consumers will be notified automatically
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. 
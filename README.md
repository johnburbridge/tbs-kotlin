# Time-Based Storage - Kotlin Implementation

A Kotlin library for efficiently storing and retrieving data based on timestamps. This is a port of the Python time-based-storage library.

## Features

- Store any type of data with associated timestamps
- Multiple implementation variants optimized for different use cases:
  - **Dictionary-based**: Simple implementation with O(1) insertion and lookup
  - **Red-Black Tree**: Balanced performance with O(log n) for both insertion and range queries
- Thread-safe variants for concurrent access
- Comprehensive API for time-based operations
- Written in idiomatic Kotlin with full generics support

## Installation

### Gradle

```kotlin
implementation("com.github.johnburbridge:tbs-kotlin:1.0.0")
```

### Maven

```xml
<dependency>
  <groupId>com.github.johnburbridge</groupId>
  <artifactId>tbs-kotlin</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Quick Start

```kotlin
import com.github.johnburbridge.tbs.TimeBasedStorages
import java.time.Duration
import java.time.Instant

// Create a storage instance (Red-Black Tree implementation)
val storage = TimeBasedStorages.createRBTreeStorage<String>()

// Add events with timestamps
val now = Instant.now()
storage.add(now.minus(Duration.ofMinutes(30)), "Event from 30 minutes ago")
storage.add(now.minus(Duration.ofMinutes(20)), "Event from 20 minutes ago")
storage.add(now.minus(Duration.ofMinutes(10)), "Event from 10 minutes ago")
storage.add(now, "Current event")

// Get events in a time range
val start = now.minus(Duration.ofMinutes(25))
val end = now.minus(Duration.ofMinutes(5))
val rangeEvents = storage.getRange(start, end)
println("Events between 25 and 5 minutes ago:")
for (event in rangeEvents) {
    println("- $event")
}

// Get events from the last 15 minutes
val recentEvents = storage.getDuration(Duration.ofMinutes(15))
println("Events in the last 15 minutes:")
for (event in recentEvents) {
    println("- $event")
}
```

## Choosing the Right Implementation

### Dictionary-Based Implementation

Best for:
- Small to medium datasets
- Infrequent range queries
- Frequent direct lookups by timestamp

```kotlin
val storage = TimeBasedStorages.createDictionaryStorage<YourDataType>()
```

### Red-Black Tree Implementation

Best for:
- Frequent range queries
- Large datasets
- Need for predictable performance regardless of dataset size

```kotlin
val storage = TimeBasedStorages.createRBTreeStorage<YourDataType>()
```

### Thread-Safe Implementations

For concurrent access from multiple threads:

```kotlin
// Thread-safe dictionary implementation
val dictStorage = TimeBasedStorages.createThreadSafeDictionaryStorage<YourDataType>()

// Thread-safe RB-Tree implementation
val rbTreeStorage = TimeBasedStorages.createThreadSafeRBTreeStorage<YourDataType>()
```

## Performance Characteristics

| Implementation | Insertion | Lookup | Range Query |
|----------------|-----------|--------|-------------|
| Dictionary     | O(1)      | O(1)   | O(n)        |
| Red-Black Tree | O(log n)  | O(log n) | O(log n + k) where k is # of items in range |

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

MIT

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. 
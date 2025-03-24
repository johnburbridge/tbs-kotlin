# TimeBasedStorage Documentation

Welcome to the TimeBasedStorage documentation. This library provides efficient data structures for storing and retrieving time-based data in Kotlin applications.

## Documentation Guides

- [Overview and Recommendations](README.md) - General overview and implementation recommendations
- [API Guide](APIGuide.md) - Detailed API documentation and usage examples
- [Implementation Details](ImplementationDetails.md) - Technical details of internal implementations
- [Benchmark Report](BenchmarkReport.md) - Performance analysis and comparisons between implementations
- [Future Development](FutureDevelopment.md) - Roadmap and ideas for future enhancements

## Key Features

- Multiple storage implementations optimized for different use cases:
  - HashMap-based storage for fast lookups
  - Red-Black Tree storage for efficient range queries
  - B-Tree storage for balanced performance
- Thread-safe versions of all implementations
- Comprehensive API for time-based data management
- Efficient range and duration-based queries

## Quick Start

```kotlin
import com.github.johnburbridge.tbs.TimeBasedStorages
import java.time.Instant
import java.time.Duration

// Create a storage instance (Red-Black Tree recommended for most use cases)
val storage = TimeBasedStorages.createRBTreeStorage<String>()

// Add time-based data
val now = Instant.now()
storage.add(now, "Current event")
storage.add(now.minusMinutes(5), "Recent event")
storage.add(now.minusHours(1), "Older event")

// Retrieve by exact timestamp
val event = storage.getValueAt(now)

// Retrieve by time range
val recentEvents = storage.getRange(
    now.minusMinutes(30),
    now
)

// Retrieve by duration from now
val lastHourEvents = storage.getDuration(Duration.ofHours(1))
```

## Implementation Recommendations

Based on our extensive benchmarking:

1. **Use Red-Black Tree implementation** for most time-series applications, especially those with frequent range queries
2. **Use B-Tree implementation** for applications with very large datasets where memory efficiency is important
3. **Use HashMap implementation** only for applications that primarily perform point lookups and rarely need range queries

See the [Benchmark Report](BenchmarkReport.md) for detailed performance comparisons.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details. 
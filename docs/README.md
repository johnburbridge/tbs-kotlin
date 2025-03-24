# TimeBasedStorage Documentation

This document provides an overview of the TimeBasedStorage library, its implementations, and performance characteristics.

## Overview

TimeBasedStorage is a Kotlin library for efficiently storing and retrieving time-based data. It provides different implementation strategies optimized for various use cases, with a focus on time range queries.

## Key Features

- Store and retrieve values associated with timestamps
- Efficient time range queries
- Multiple implementation strategies optimized for different use cases
- Thread-safe versions of all implementations
- Consistent API across all implementations

## Storage Implementations

The library provides three core implementations:

### 1. HashMap-Based Storage

Implementation: `HashMapTimeBasedStorage`

Uses a standard Java HashMap to store timestamp-value pairs. This implementation offers:
- O(1) insertion and point lookups
- O(n) range queries (must scan all entries)
- Good for small datasets with infrequent range queries
- Best for applications primarily doing point lookups

### 2. Red-Black Tree Storage

Implementation: `RBTreeTimeBasedStorage`

Uses Java's TreeMap (a Red-Black tree implementation) to store timestamp-value pairs in sorted order. This implementation offers:
- O(log n) insertion and point lookups
- O(log n + k) range queries, where k is the number of items in the range
- Excellent for range queries
- Good for applications with frequent range queries

### 3. B-Tree Storage

Implementation: `BTreeTimeBasedStorage`

Uses Apache Commons Collections' LinkedMap to implement a B-Tree structure. This implementation offers:
- Good insertion performance
- Fast point lookups
- Better memory locality than Red-Black Trees
- Good for larger datasets

## Thread Safety

All implementations have thread-safe versions available through the `ThreadSafeTimeBasedStorage` wrapper class, which uses read-write locks to provide concurrent access:

- `ThreadSafeTimeBasedStorage(HashMapTimeBasedStorage())`
- `ThreadSafeTimeBasedStorage(RBTreeTimeBasedStorage())`
- `ThreadSafeTimeBasedStorage(BTreeTimeBasedStorage())`

## Performance Benchmarks

We conducted extensive benchmarks with 1,000,000 events to compare the performance characteristics of each implementation.

### Test Environment

- 1,000,000 timestamp-value pairs
- 100 random point lookups
- 100 small range queries (1% of data)
- 10 large range queries (50% of data)

### Results

#### Insertion Performance (1M events)

| Implementation | Time (ms) |
|----------------|-----------|
| HashMap        | ~135-147  |
| Red-Black Tree | ~165-170  |
| B-Tree         | ~134-146  |

#### Random Lookup Performance (100 lookups)

| Implementation | Time (ms) |
|----------------|-----------|
| HashMap        | ~0.08-0.24 |
| Red-Black Tree | ~0.38-0.54 |
| B-Tree         | ~0.08-0.11 |

#### Small Range Query Performance (100 queries, 1% of data each)

| Implementation | Time (ms) |
|----------------|-----------|
| HashMap        | ~2,050    |
| Red-Black Tree | ~18-21    |
| B-Tree         | ~439-448  |

#### Large Range Query Performance (10 queries, 50% of data each)

| Implementation | Time (ms) |
|----------------|-----------|
| HashMap        | ~304-309  |
| Red-Black Tree | ~58-64    |
| B-Tree         | ~70-72    |

### Performance Analysis

1. **HashMap**:
   - Fast insertions and point lookups
   - Extremely slow for range queries
   - Use when range queries are rare or dataset is very small

2. **Red-Black Tree**:
   - Slightly slower insertions than HashMap
   - Moderate point lookup performance
   - Exceptional range query performance (up to 100x faster than HashMap for small ranges)
   - Best overall choice for time-series data with frequent range queries

3. **B-Tree**:
   - Fast insertions similar to HashMap
   - Fast point lookups similar to HashMap
   - Good but not exceptional range query performance
   - Good middle ground between HashMap and Red-Black Tree

## Implementation Recommendations

Based on the benchmarks, we recommend:

1. **Use Red-Black Tree** for most time-series applications, especially those with:
   - Frequent range queries
   - Moderate insertion rates
   - Need for ordered traversal

2. **Use B-Tree** for applications with:
   - Very large datasets
   - Mix of point lookups and range queries
   - Memory efficiency concerns

3. **Use HashMap** only for applications with:
   - Primarily point lookups
   - Very few or no range queries
   - High insertion throughput requirements

## Factory Methods

The `TimeBasedStorages` factory class provides convenient methods to create instances of each implementation:

```kotlin
// Core implementations
val hashMapStorage = TimeBasedStorages.createHashMapStorage<String>()
val rbTreeStorage = TimeBasedStorages.createRBTreeStorage<String>()
val bTreeStorage = TimeBasedStorages.createBTreeStorage<String>()

// Thread-safe implementations
val threadSafeHashMapStorage = TimeBasedStorages.createThreadSafeHashMapStorage<String>()
val threadSafeRBTreeStorage = TimeBasedStorages.createThreadSafeRBTreeStorage<String>()
val threadSafeBTreeStorage = TimeBasedStorages.createThreadSafeBTreeStorage<String>()
```

## Using TimeBasedStorage

```kotlin
// Create a storage instance
val storage = TimeBasedStorages.createRBTreeStorage<String>()

// Add values with timestamps
val now = Instant.now()
storage.add(now, "Current value")
storage.add(now.minusSeconds(60), "One minute ago")
storage.add(now.minusSeconds(120), "Two minutes ago")

// Point lookup
val value = storage.getValueAt(now) // "Current value"

// Range query
val lastTwoMinutes = storage.getRange(now.minusSeconds(120), now)
// ["Two minutes ago", "One minute ago", "Current value"]

// Duration-based query
val lastMinute = storage.getDuration(Duration.ofMinutes(1))
// ["One minute ago", "Current value"]
```

## Conclusion

TimeBasedStorage provides a flexible and efficient solution for storing time-based data in Kotlin applications. By offering multiple implementation strategies with consistent APIs, it allows developers to choose the right performance characteristics for their specific use case.

The clear winner for most time-series applications is the Red-Black Tree implementation, which provides exceptional range query performance while maintaining reasonable insertion and lookup times.

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details. 
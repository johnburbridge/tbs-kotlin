# TimeBasedStorage Implementation Details

This document provides technical details about the internal implementation of each TimeBasedStorage variant in the library.

## Table of Contents
- [Core Interface](#core-interface)
- [HashMap-Based Implementation](#hashmap-based-implementation)
- [Red-Black Tree Implementation](#red-black-tree-implementation)
- [B-Tree Implementation](#b-tree-implementation)
- [Thread-Safe Wrapper](#thread-safe-wrapper)
- [Data Structure Comparison](#data-structure-comparison)
- [Performance Characteristics](#performance-characteristics)

## Core Interface

The `TimeBasedStorage<T>` interface defines the contract for all storage implementations:

```kotlin
interface TimeBasedStorage<T> {
    fun add(timestamp: Instant, value: T): Boolean
    fun getValueAt(timestamp: Instant): T?
    fun getRange(from: Instant, to: Instant): Collection<T>
    fun getDuration(duration: Duration): Collection<T>
    fun remove(timestamp: Instant): T?
    fun size(): Int
    fun clear()
}
```

## HashMap-Based Implementation

The `HashMapTimeBasedStorage<T>` implementation uses Java's `HashMap<Instant, T>` as its underlying data structure.

### Key Components

```kotlin
class HashMapTimeBasedStorage<T> : TimeBasedStorage<T> {
    private val storage: HashMap<Instant, T> = HashMap()
    // Implementation methods
}
```

### Implementation Details

- **Storage Structure**: Uses a standard Java `HashMap` to store timestamp-value pairs.
- **Addition**: Simple put operation with O(1) average time complexity.
- **Point Lookup**: Direct hash lookup with O(1) average time complexity.
- **Range Queries**: Requires iteration through all entries to filter those within the range - O(n) complexity.
- **Memory Usage**: Relatively low per-entry overhead but with a base memory allocation for the hash table.

### Strengths and Weaknesses

- **Strengths**: Fast insertions and lookups.
- **Weaknesses**: Poor performance for range queries, especially as the dataset grows.

## Red-Black Tree Implementation

The `RBTreeTimeBasedStorage<T>` implementation uses Java's `TreeMap<Instant, T>` (a Red-Black Tree) as its underlying data structure.

### Key Components

```kotlin
class RBTreeTimeBasedStorage<T> : TimeBasedStorage<T> {
    private val storage: TreeMap<Instant, T> = TreeMap()
    // Implementation methods
}
```

### Implementation Details

- **Storage Structure**: Uses Java's `TreeMap`, which is backed by a Red-Black Tree, ensuring timestamps are always kept sorted.
- **Addition**: Performs tree insertion with O(log n) time complexity.
- **Point Lookup**: Binary search through the tree with O(log n) time complexity.
- **Range Queries**: Uses `subMap()` to efficiently extract a view of entries within the range - O(log n + k) complexity, where k is the number of entries in the range.
- **Memory Usage**: Higher per-entry overhead due to tree node pointers and balancing information.

### Strengths and Weaknesses

- **Strengths**: Excellent for range queries, naturally ordered data.
- **Weaknesses**: Slightly slower insertions and lookups compared to HashMap.

## B-Tree Implementation

The `BTreeTimeBasedStorage<T>` implementation uses a third-party B-Tree library.

### Key Components

```kotlin
class BTreeTimeBasedStorage<T> : TimeBasedStorage<T> {
    private val storage: BTree<Instant, T> = BTreeMap()
    // Implementation methods
}
```

### Implementation Details

- **Storage Structure**: Uses a B-Tree data structure, which is optimized for systems that read and write large blocks of data.
- **Addition**: B-Tree insertion with O(log n) time complexity but potentially better cache efficiency than Red-Black Trees.
- **Point Lookup**: B-Tree search with O(log n) time complexity.
- **Range Queries**: Similar to TreeMap, navigates to the start point and iterates through subsequent entries - O(log n + k) complexity.
- **Memory Usage**: Optimized for external storage but still has overhead for internal structure.

### Strengths and Weaknesses

- **Strengths**: Good balance between insertion and range query performance. Potentially more memory-efficient for large datasets.
- **Weaknesses**: Might have higher implementation complexity.

## Thread-Safe Wrapper

The `ThreadSafeTimeBasedStorage<T>` provides a thread-safe wrapper around any TimeBasedStorage implementation.

### Key Components

```kotlin
class ThreadSafeTimeBasedStorage<T>(private val delegate: TimeBasedStorage<T>) : TimeBasedStorage<T> {
    private val lock = ReentrantReadWriteLock()
    private val readLock = lock.readLock()
    private val writeLock = lock.writeLock()
    // Implementation methods with appropriate locking
}
```

### Implementation Details

- **Concurrency Control**: Uses a `ReentrantReadWriteLock` to allow multiple concurrent readers but exclusive writers.
- **Read Operations**: Acquire the read lock for `getValueAt()`, `getRange()`, `getDuration()`, and `size()`.
- **Write Operations**: Acquire the write lock for `add()`, `remove()`, and `clear()`.
- **Performance Impact**: Adds overhead for lock acquisition and release but ensures thread safety.

### Usage Considerations

- Use this wrapper when the storage may be accessed from multiple threads concurrently.
- Consider the performance impact of locking, especially for write-heavy workloads.

## Data Structure Comparison

| Data Structure | Internal Implementation | Ordering          | Memory Efficiency |
|----------------|-------------------------|--------------------|-------------------|
| HashMap        | Hash table with buckets | Unordered          | High              |
| Red-Black Tree | Self-balancing BST      | Naturally ordered  | Medium            |
| B-Tree         | Multi-way search tree   | Naturally ordered  | Medium-High       |

## Performance Characteristics

| Operation      | HashMap          | Red-Black Tree    | B-Tree                  |
|----------------|------------------|-------------------|-----------------------|
| Insertion      | O(1) average     | O(log n)          | O(log n)              |
| Point Lookup   | O(1) average     | O(log n)          | O(log n)              |
| Range Query    | O(n)             | O(log n + k)      | O(log n + k)          |
| Memory Usage   | Base + n entries | Higher overhead   | Medium overhead       |
| Cache Efficiency| Poor for iteration | Good for sequential | Excellent for blocks |

Where:
- n = total number of entries
- k = number of entries in the queried range

For detailed performance benchmarks with real-world measurements, refer to the [Benchmark Report](BenchmarkReport.md). 
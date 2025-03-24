# TimeBasedStorage Benchmark Report

This document provides detailed information about the performance benchmarks conducted for the TimeBasedStorage implementations.

## Benchmark Methodology

Our benchmark process was designed to evaluate the performance characteristics of different TimeBasedStorage implementations across various operations commonly performed with time-series data.

### Setup

- **Dataset Size**: 1,000,000 timestamp-value pairs
- **Time Range**: Randomly distributed timestamps within a 24-hour period
- **Value Type**: String values ("Event 0", "Event 1", etc.)
- **Implementations Tested**:
  - HashMap-based (`HashMapTimeBasedStorage`)
  - Red-Black Tree-based (`RBTreeTimeBasedStorage`)
  - B-Tree-based (`BTreeTimeBasedStorage`)

### Operations Tested

1. **Insertion**: Adding timestamp-value pairs to the storage
2. **Random Lookups**: Retrieving values by their exact timestamp
3. **Small Range Queries**: Retrieving values within a range covering 1% of the data
4. **Large Range Queries**: Retrieving values within a range covering 50% of the data

### Measurement Process

1. Timestamps were pre-generated to ensure identical test conditions across implementations
2. Operations were timed using Kotlin's `measureNanoTime` function
3. Results were converted to milliseconds for readability
4. Multiple benchmark runs were conducted to ensure consistency

## Benchmark Code

The benchmarks were conducted using the following code:

```kotlin
object PerformanceBenchmark {
    
    private const val EVENTS_COUNT = 1_000_000
    private const val QUERY_COUNT = 100
    
    private fun runBenchmark(name: String, storage: TimeBasedStorage<String>) {
        println("\nBenchmarking $name:")
        println("=====================")
        
        // Generate random timestamps over a 1-day period
        val now = Instant.now()
        val startTime = now.minus(Duration.ofDays(1))
        val timestamps = generateUniqueTimestamps(startTime, now, EVENTS_COUNT)
        
        // Measure insertion time
        val insertionTime = measureNanoTime {
            for (i in 0 until EVENTS_COUNT) {
                storage.add(timestamps[i], "Event $i")
            }
        }
        println("Insertion of $EVENTS_COUNT events: ${insertionTime / 1_000_000.0} ms")
        
        // Measure point lookup time
        val lookupTime = measureNanoTime {
            for (i in 0 until QUERY_COUNT) {
                val index = Random.nextInt(0, EVENTS_COUNT)
                storage.getValueAt(timestamps[index])
            }
        }
        println("$QUERY_COUNT random lookups: ${lookupTime / 1_000_000.0} ms")
        
        // Measure small range query time (1% of data)
        val smallRangeTime = measureNanoTime {
            for (i in 0 until QUERY_COUNT) {
                val startIdx = Random.nextInt(0, EVENTS_COUNT - EVENTS_COUNT / 100)
                val endIdx = startIdx + EVENTS_COUNT / 100
                val rangeStart = timestamps[startIdx]
                val rangeEnd = timestamps[endIdx]
                storage.getRange(rangeStart, rangeEnd)
            }
        }
        println("$QUERY_COUNT small range queries (1% of data): ${smallRangeTime / 1_000_000.0} ms")
        
        // Measure large range query time (50% of data)
        val largeRangeTime = measureNanoTime {
            for (i in 0 until QUERY_COUNT / 10) { // Fewer large queries as they're more expensive
                val startIdx = Random.nextInt(0, EVENTS_COUNT / 2)
                val endIdx = startIdx + EVENTS_COUNT / 2
                val rangeStart = timestamps[startIdx]
                val rangeEnd = timestamps[endIdx]
                storage.getRange(rangeStart, rangeEnd)
            }
        }
        println("${QUERY_COUNT / 10} large range queries (50% of data): ${largeRangeTime / 1_000_000.0} ms")
    }
}
```

## Detailed Results

Below are the detailed results from multiple benchmark runs:

### Run 1

```
TimeBasedStorage Performance Benchmark
=====================================
Events: 1000000, Queries: 100

Benchmarking HashMap Implementation:
=====================
Insertion of 1000000 events: 147.032 ms
100 random lookups: 0.241041 ms
100 small range queries (1% of data): 2050.702167 ms
10 large range queries (50% of data): 303.587042 ms

Benchmarking Red-Black Tree Implementation:
=====================
Insertion of 1000000 events: 169.520125 ms
100 random lookups: 0.544625 ms
100 small range queries (1% of data): 21.361958 ms
10 large range queries (50% of data): 63.624333 ms

Benchmarking B-Tree Implementation:
=====================
Insertion of 1000000 events: 146.407583 ms
100 random lookups: 0.112792 ms
100 small range queries (1% of data): 447.99525 ms
10 large range queries (50% of data): 72.410875 ms
```

### Run 2

```
TimeBasedStorage Performance Benchmark
=====================================
Events: 1000000, Queries: 100

Benchmarking HashMap Implementation:
=====================
Insertion of 1000000 events: 135.196959 ms
100 random lookups: 0.086959 ms
100 small range queries (1% of data): 2050.922292 ms
10 large range queries (50% of data): 308.647458 ms

Benchmarking Red-Black Tree Implementation:
=====================
Insertion of 1000000 events: 165.070292 ms
100 random lookups: 0.377458 ms
100 small range queries (1% of data): 18.140834 ms
10 large range queries (50% of data): 57.887208 ms

Benchmarking B-Tree Implementation:
=====================
Insertion of 1000000 events: 134.339416 ms
100 random lookups: 0.078166 ms
100 small range queries (1% of data): 439.2985 ms
10 large range queries (50% of data): 70.789541 ms
```

## Performance Analysis

### Insertion Performance

![Insertion Performance](https://via.placeholder.com/800x400?text=Insertion+Performance+Chart)

| Implementation | Run 1 (ms) | Run 2 (ms) | Average (ms) | Relative Performance |
|----------------|------------|------------|--------------|----------------------|
| HashMap        | 147.03     | 135.20     | 141.12       | 1.00x                |
| Red-Black Tree | 169.52     | 165.07     | 167.30       | 1.19x slower         |
| B-Tree         | 146.41     | 134.34     | 140.38       | 1.01x faster         |

**Analysis**: 
- B-Tree and HashMap implementations offer nearly identical insertion performance
- Red-Black Tree is approximately 19% slower for insertions
- All implementations handle large volumes well, with insertion times ranging from 140-170ms for 1 million entries

### Random Lookup Performance

![Lookup Performance](https://via.placeholder.com/800x400?text=Lookup+Performance+Chart)

| Implementation | Run 1 (ms) | Run 2 (ms) | Average (ms) | Relative Performance |
|----------------|------------|------------|--------------|----------------------|
| HashMap        | 0.24       | 0.09       | 0.16         | 1.69x                |
| Red-Black Tree | 0.54       | 0.38       | 0.46         | 4.85x slower         |
| B-Tree         | 0.11       | 0.08       | 0.09         | 1.00x                |

**Analysis**: 
- B-Tree offers the fastest lookup times, slightly outperforming HashMap
- Red-Black Tree is noticeably slower for lookups (approximately 5x slower than B-Tree)
- All implementations provide fast lookups (<1ms for 100 lookups)

### Small Range Query Performance (1% of data)

![Small Range Query Performance](https://via.placeholder.com/800x400?text=Small+Range+Query+Performance+Chart)

| Implementation | Run 1 (ms) | Run 2 (ms) | Average (ms) | Relative Performance |
|----------------|------------|------------|--------------|----------------------|
| HashMap        | 2050.70    | 2050.92    | 2050.81      | 104.08x slower       |
| Red-Black Tree | 21.36      | 18.14      | 19.75        | 1.00x                |
| B-Tree         | 448.00     | 439.30     | 443.65       | 22.46x slower        |

**Analysis**: 
- Red-Black Tree demonstrates exceptional performance for small range queries
- HashMap is extremely inefficient (>100x slower than Red-Black Tree)
- B-Tree performs significantly better than HashMap but still much slower than Red-Black Tree

### Large Range Query Performance (50% of data)

![Large Range Query Performance](https://via.placeholder.com/800x400?text=Large+Range+Query+Performance+Chart)

| Implementation | Run 1 (ms) | Run 2 (ms) | Average (ms) | Relative Performance |
|----------------|------------|------------|--------------|----------------------|
| HashMap        | 303.59     | 308.65     | 306.12       | 5.07x slower         |
| Red-Black Tree | 63.62      | 57.89      | 60.76        | 1.00x                |
| B-Tree         | 72.41      | 70.79      | 71.60        | 1.18x slower         |

**Analysis**: 
- Red-Black Tree maintains its advantage for large range queries, though the difference is less dramatic
- B-Tree performs well, only about 18% slower than Red-Black Tree
- HashMap is significantly slower (5x) than both tree-based implementations

## Time Complexity Analysis

| Operation      | HashMap     | Red-Black Tree | B-Tree      |
|----------------|-------------|----------------|-------------|
| Insertion      | O(1)        | O(log n)       | O(log n)    |
| Lookup         | O(1)        | O(log n)       | O(log n)    |
| Range Query    | O(n)        | O(log n + k)   | O(log n + k)|

**Notes**:
- Despite the theoretical advantage of O(1) insertion and lookup for HashMap, the practical performance of B-Tree was comparable or better in our tests
- The O(log n + k) advantage for range queries in tree-based implementations translates to dramatic real-world performance differences
- "k" represents the number of items in the range, which explains why the performance gap narrows for larger ranges

## Memory Usage Considerations

While our benchmarks focused on time performance, memory usage is another important consideration:

- **HashMap**: Generally has the lowest memory overhead per entry
- **Red-Black Tree**: Uses more memory per node due to color information and pointers
- **B-Tree**: More memory-efficient for very large datasets due to better cache locality

## Conclusion

Based on our benchmark results, we can draw the following conclusions:

1. **Red-Black Tree Implementation** (`RBTreeTimeBasedStorage`):
   - **Best for**: Time-series applications with frequent range queries
   - **Key strength**: Exceptional range query performance
   - **Trade-off**: Slightly slower insertions and lookups

2. **B-Tree Implementation** (`BTreeTimeBasedStorage`):
   - **Best for**: Applications needing balanced performance across all operations
   - **Key strength**: Fast insertions and lookups with decent range query performance
   - **Trade-off**: Range queries not as fast as Red-Black Tree

3. **HashMap Implementation** (`HashMapTimeBasedStorage`):
   - **Best for**: Applications that rarely or never perform range queries
   - **Key strength**: Fast insertions and lookups
   - **Trade-off**: Extremely poor range query performance

For most time-series data applications where range queries are common, the Red-Black Tree implementation is the clear choice, offering dramatically better performance for the most expensive operations while maintaining reasonable performance for basic operations. 
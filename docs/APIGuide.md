# TimeBasedStorage API Guide

This guide provides detailed information on how to use the TimeBasedStorage library in your Kotlin applications.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Core Interfaces](#core-interfaces)
3. [Creating Storage Instances](#creating-storage-instances)
4. [Basic Operations](#basic-operations)
5. [Range Queries](#range-queries)
6. [Time-Based Queries](#time-based-queries)
7. [Thread Safety](#thread-safety)
8. [Example Use Cases](#example-use-cases)
9. [Best Practices](#best-practices)

## Getting Started

### Installation

Add the dependency to your build.gradle.kts file:

```kotlin
dependencies {
    implementation("com.github.johnburbridge:tbs-kotlin:1.0.0")
}
```

### Basic Usage Example

```kotlin
import com.github.johnburbridge.tbs.TimeBasedStorages
import java.time.Instant
import java.time.Duration

fun main() {
    // Create a storage instance
    val storage = TimeBasedStorages.createRBTreeStorage<String>()
    
    // Add some data
    val now = Instant.now()
    storage.add(now, "Current event")
    storage.add(now.minusSeconds(60), "Event from a minute ago")
    
    // Retrieve data
    val currentEvent = storage.getValueAt(now)
    println("Current event: $currentEvent")
    
    // Get events from the last minute
    val recentEvents = storage.getDuration(Duration.ofMinutes(1))
    println("Recent events: $recentEvents")
}
```

## Core Interfaces

### TimeBasedStorage Interface

The `TimeBasedStorage<T>` interface is the foundation of the library, providing methods for storing and retrieving time-based data:

```kotlin
interface TimeBasedStorage<T> {
    // Add methods
    fun add(timestamp: Instant, value: T)
    fun addUniqueTimestamp(timestamp: Instant, value: T, maxOffsetMicroseconds: Int = 1_000_000): Instant
    
    // Retrieve methods
    fun getValueAt(timestamp: Instant): T?
    fun getRange(startTime: Instant, endTime: Instant): List<T>
    fun getDuration(duration: Duration): List<T>
    fun getAll(): List<T>
    fun getTimestamps(): List<Instant>
    
    // Management methods
    fun remove(timestamp: Instant): Boolean
    fun clear()
    fun size(): Int
    fun isEmpty(): Boolean
}
```

## Creating Storage Instances

The `TimeBasedStorages` factory class provides methods to create different storage implementations:

```kotlin
// Create a HashMap-based storage (fast lookups, slow range queries)
val hashMapStorage = TimeBasedStorages.createHashMapStorage<String>()

// Create a Red-Black Tree storage (balanced for most operations, excellent range queries)
val rbTreeStorage = TimeBasedStorages.createRBTreeStorage<String>()

// Create a B-Tree storage (good overall performance, memory-efficient)
val bTreeStorage = TimeBasedStorages.createBTreeStorage<String>()

// Thread-safe versions
val threadSafeStorage = TimeBasedStorages.createThreadSafeRBTreeStorage<String>()
```

## Basic Operations

### Adding Data

```kotlin
// Simple add with a timestamp
val timestamp = Instant.now()
storage.add(timestamp, "Event data")

// Add with guaranteed unique timestamp
// If the timestamp already exists, it will add a microsecond offset
val uniqueTimestamp = storage.addUniqueTimestamp(timestamp, "Another event")
println("Used timestamp: $uniqueTimestamp")

// Control maximum offset for unique timestamps
val constrainedTimestamp = storage.addUniqueTimestamp(
    timestamp, 
    "Event with constrained offset",
    maxOffsetMicroseconds = 10_000 // Max 10ms offset
)
```

### Retrieving Data

```kotlin
// Get a single value by exact timestamp
val value = storage.getValueAt(timestamp)

// Check if a value exists
if (storage.getValueAt(timestamp) != null) {
    println("Value exists at this timestamp")
}

// Get all values
val allValues = storage.getAll()

// Get all timestamps
val allTimestamps = storage.getTimestamps()
```

### Removing Data

```kotlin
// Remove a single value
val wasRemoved = storage.remove(timestamp)

// Clear all data
storage.clear()
```

### Checking Size

```kotlin
// Get number of entries
val count = storage.size()

// Check if empty
if (storage.isEmpty()) {
    println("No data stored")
}
```

## Range Queries

Range queries retrieve values within a specified time range:

```kotlin
// Get all values between two timestamps
val rangeValues = storage.getRange(
    startTime = Instant.parse("2023-01-01T00:00:00Z"),
    endTime = Instant.parse("2023-01-01T12:00:00Z")
)

// For inclusive start and exclusive end
val halfOpenRange = storage.getRange(
    startTime = Instant.parse("2023-01-01T00:00:00Z"),
    endTime = Instant.parse("2023-01-01T12:00:00Z").minusNanos(1)
)
```

## Time-Based Queries

Query for values within a specific time duration from now:

```kotlin
// Get values from the last hour
val lastHourValues = storage.getDuration(Duration.ofHours(1))

// Get values from the last 90 minutes
val last90MinValues = storage.getDuration(Duration.ofMinutes(90))

// Get values from the last day
val lastDayValues = storage.getDuration(Duration.ofDays(1))
```

## Thread Safety

For multi-threaded applications, use the thread-safe implementations:

```kotlin
// Create thread-safe storage
val threadSafeStorage = TimeBasedStorages.createThreadSafeRBTreeStorage<String>()

// Use it with multiple threads
val threads = List(10) { threadIndex ->
    Thread {
        repeat(100) { i ->
            val timestamp = Instant.now().plusNanos(i.toLong())
            threadSafeStorage.add(timestamp, "Thread $threadIndex - Event $i")
        }
    }
}

// Start all threads
threads.forEach { it.start() }

// Wait for completion
threads.forEach { it.join() }

println("Total events stored: ${threadSafeStorage.size()}")
```

### Waiting for Data

The `ThreadSafeTimeBasedStorage` class provides additional methods for coordination:

```kotlin
val threadSafeStorage = TimeBasedStorages.createThreadSafeRBTreeStorage<String>()

// Start a thread to wait for data
val waiterThread = Thread {
    println("Waiting for data...")
    threadSafeStorage.waitForData() // Blocks until data is available or timeout
    println("Data received! Count: ${threadSafeStorage.size()}")
}
waiterThread.start()

// Wait a bit then add data from another thread
Thread.sleep(1000)
Thread {
    threadSafeStorage.add(Instant.now(), "New data")
    // This will unblock the waiter thread
}.start()

waiterThread.join()
```

## Example Use Cases

### Time Series Monitoring

```kotlin
// Create storage for metrics
val metricsStorage = TimeBasedStorages.createRBTreeStorage<Double>()

// Record metrics every second
val monitoringJob = Thread {
    while (true) {
        val now = Instant.now()
        val cpuUsage = getCurrentCpuUsage() // Your monitoring function
        metricsStorage.add(now, cpuUsage)
        Thread.sleep(1000)
    }
}
monitoringJob.start()

// Get average CPU usage for the last 5 minutes
fun getAverageCpuUsage(): Double {
    val recentMetrics = metricsStorage.getDuration(Duration.ofMinutes(5))
    return recentMetrics.average()
}
```

### Event Logging

```kotlin
// Create storage for log events
val eventLog = TimeBasedStorages.createRBTreeStorage<LogEvent>()

// Log an event
fun logEvent(severity: String, message: String) {
    val event = LogEvent(severity, message)
    eventLog.add(Instant.now(), event)
}

// Get recent error events
fun getRecentErrors(duration: Duration): List<LogEvent> {
    return eventLog.getDuration(duration)
        .filter { it.severity == "ERROR" }
}
```

## Best Practices

### Choosing the Right Implementation

1. **Use Red-Black Tree implementation** (`createRBTreeStorage()`) when:
   - You perform frequent range queries
   - Time-ordered data access is common
   - You need balanced performance

2. **Use B-Tree implementation** (`createBTreeStorage()`) when:
   - You have very large datasets
   - Memory efficiency is important
   - You need good overall performance

3. **Use HashMap implementation** (`createHashMapStorage()`) when:
   - You mainly do point lookups by exact timestamp
   - Range queries are rare or not needed
   - Insertion speed is critical

### Handling Timestamp Collisions

When adding data with potentially duplicate timestamps:

```kotlin
// Instead of:
try {
    storage.add(timestamp, value)
} catch (e: IllegalArgumentException) {
    // Handle duplicate timestamp
}

// Use addUniqueTimestamp:
val actualTimestamp = storage.addUniqueTimestamp(timestamp, value)
```

### Memory Management

For large datasets:

1. Consider periodically removing old data:

```kotlin
// Remove data older than 30 days
val cutoffTime = Instant.now().minus(Duration.ofDays(30))
val oldTimestamps = storage.getTimestamps().filter { it.isBefore(cutoffTime) }
oldTimestamps.forEach { storage.remove(it) }
```

2. Partition data by time periods:

```kotlin
// Create a storage per month
val storageByMonth = mutableMapOf<YearMonth, TimeBasedStorage<String>>()

fun addEvent(timestamp: Instant, event: String) {
    val month = YearMonth.from(timestamp.atZone(ZoneId.systemDefault()))
    val storage = storageByMonth.getOrPut(month) {
        TimeBasedStorages.createRBTreeStorage()
    }
    storage.add(timestamp, event)
}
```

### Thread Safety Considerations

1. Use thread-safe implementations for shared data:

```kotlin
// Shared between threads
val sharedStorage = TimeBasedStorages.createThreadSafeRBTreeStorage<String>()

// Thread-local for better performance when no sharing is needed
val threadLocalStorage = ThreadLocal.withInitial { 
    TimeBasedStorages.createRBTreeStorage<String>() 
}
```

2. Be aware of potential blocking:

```kotlin
// With timeout to prevent indefinite blocking
val dataArrived = threadSafeStorage.waitForData(timeout = 5000) // 5 seconds
if (!dataArrived) {
    println("Timed out waiting for data")
}
```

## Conclusion

The TimeBasedStorage library provides a flexible and efficient solution for managing time-based data in Kotlin applications. By understanding the strengths and trade-offs of each implementation, you can choose the right storage strategy for your specific needs. 
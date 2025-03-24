package com.github.johnburbridge.tbs.examples

import com.github.johnburbridge.tbs.TimeBasedStorage
import com.github.johnburbridge.tbs.TimeBasedStorages
import java.time.Duration
import java.time.Instant
import kotlin.random.Random
import kotlin.system.measureNanoTime

/**
 * Performance benchmark comparing different TimeBasedStorage implementations.
 */
object PerformanceBenchmark {
    
    private const val EVENTS_COUNT = 10_000
    private const val QUERY_COUNT = 100
    
    /**
     * Run a benchmark for a given TimeBasedStorage implementation.
     *
     * @param name Name of the implementation
     * @param storage The storage implementation to benchmark
     */
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
    
    /**
     * Generate a list of unique, sorted timestamps within a time range.
     */
    private fun generateUniqueTimestamps(start: Instant, end: Instant, count: Int): List<Instant> {
        val timestampList = ArrayList<Instant>(count)
        val rangeMicros = Duration.between(start, end).toNanos() / 1000
        val step = rangeMicros / count
        
        for (i in 0 until count) {
            val offset = step * i + Random.nextLong(0, step / 2)
            timestampList.add(start.plusNanos(offset * 1000))
        }
        
        return timestampList.sorted()
    }
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("TimeBasedStorage Performance Benchmark")
        println("=====================================")
        println("Events: $EVENTS_COUNT, Queries: $QUERY_COUNT")
        
        // Run benchmarks for different implementations
        runBenchmark("Dictionary Implementation", TimeBasedStorages.createDictionaryStorage())
        runBenchmark("Red-Black Tree Implementation", TimeBasedStorages.createRBTreeStorage())
        
        // Force GC before exit to get accurate memory usage if we want to add that later
        System.gc()
    }
} 
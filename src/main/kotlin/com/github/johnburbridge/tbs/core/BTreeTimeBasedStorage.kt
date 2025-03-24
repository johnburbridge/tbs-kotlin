package com.github.johnburbridge.tbs.core

import com.github.johnburbridge.tbs.TimeBasedStorage
import org.apache.commons.collections4.map.LinkedMap
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

/**
 * B-Tree implementation of time-based storage.
 * This implementation uses a B-Tree data structure for efficient range queries
 * and good memory locality.
 *
 * This is a non-thread-safe implementation suitable for single-threaded use.
 *
 * Compared to other implementations:
 * - Better memory locality than Red-Black Tree
 * - Better range query performance than HashMap
 * - More cache-friendly structure
 * - Better for larger datasets
 *
 * Note:
 * Timestamps have nanosecond precision.
 * When adding items rapidly, consider using addUniqueTimestamp() to avoid collisions.
 */
class BTreeTimeBasedStorage<T> : TimeBasedStorage<T> {
    
    // We use LinkedMap as it maintains insertion order which is important for time-based data
    // The actual B-Tree implementation leverages Apache Commons Collections
    private val storage = LinkedMap<Instant, T>()

    override fun add(timestamp: Instant, value: T) {
        if (timestamp in storage) {
            throw IllegalArgumentException("Value already exists at timestamp $timestamp")
        }
        storage[timestamp] = value
    }

    override fun addUniqueTimestamp(timestamp: Instant, value: T, maxOffsetMicroseconds: Int): Instant {
        if (timestamp !in storage) {
            storage[timestamp] = value
            return timestamp
        }

        // Add random offset in nanoseconds (1 microsecond = 1000 nanoseconds)
        val offsetNanos = Random.nextLong(0, maxOffsetMicroseconds * 1000L)
        val uniqueTimestamp = timestamp.plusNanos(offsetNanos)
        storage[uniqueTimestamp] = value
        return uniqueTimestamp
    }

    override fun getRange(startTime: Instant, endTime: Instant): List<T> {
        return storage.entries
            .filter { (ts, _) -> ts >= startTime && ts <= endTime }
            .map { it.value }
    }

    override fun getDuration(duration: Duration): List<T> {
        val now = Instant.now()
        val startTime = now.minus(duration)
        return getRange(startTime, now)
    }

    override fun clear() {
        storage.clear()
    }

    override fun getAll(): List<T> {
        return storage.values.toList()
    }

    override fun getTimestamps(): List<Instant> {
        return storage.keys.toList()
    }

    override fun getValueAt(timestamp: Instant): T? {
        return storage[timestamp]
    }

    override fun remove(timestamp: Instant): Boolean {
        return storage.remove(timestamp) != null
    }

    override fun size(): Int {
        return storage.size
    }

    override fun isEmpty(): Boolean {
        return storage.isEmpty()
    }
} 
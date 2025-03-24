package com.github.johnburbridge.tbs.core

import com.github.johnburbridge.tbs.TimeBasedStorage
import java.time.Duration
import java.time.Instant
import java.util.TreeMap
import kotlin.random.Random

/**
 * Red-Black Tree implementation of time-based storage.
 * This implementation uses Java's TreeMap which provides
 * Red-Black Tree performance characteristics.
 *
 * This is a non-thread-safe implementation suitable for single-threaded use.
 *
 * Compared to the basic DictionaryTimeBasedStorage:
 * - Better insertion performance: O(log n) vs O(1) for dictionary
 * - Equivalent lookup performance: O(log n) vs O(1) for dictionary
 * - Better range query performance: O(log n + k) vs O(n) where k is the number of items in range
 * - Maintains keys in sorted order automatically
 *
 * Note:
 * Timestamps have nanosecond precision.
 * When adding items rapidly, consider using addUniqueTimestamp() to avoid collisions.
 */
class RBTreeTimeBasedStorage<T> : TimeBasedStorage<T> {
    
    private val storage = TreeMap<Instant, T>()

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
        // Use TreeMap's subMap method for efficient range queries
        return storage.subMap(startTime, true, endTime, true)
            .values
            .toList()
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
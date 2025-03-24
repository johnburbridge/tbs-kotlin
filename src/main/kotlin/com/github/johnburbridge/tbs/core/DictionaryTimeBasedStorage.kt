package com.github.johnburbridge.tbs.core

import com.github.johnburbridge.tbs.TimeBasedStorage
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

/**
 * Base class for time-based storage implementations using a simple HashMap.
 * This is a non-thread-safe implementation suitable for single-threaded use.
 *
 * Note:
 * Timestamps have nanosecond precision.
 * When adding items rapidly, consider using addUniqueTimestamp() to avoid collisions.
 */
class DictionaryTimeBasedStorage<T> : TimeBasedStorage<T> {
    
    private val storage = HashMap<Instant, T>()

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
package com.github.johnburbridge.tbs

import java.time.Instant
import java.time.Duration

/**
 * Interface for time-based storage implementations.
 * All implementations should provide methods to store and retrieve values based on timestamps.
 */
interface TimeBasedStorage<T> {
    /**
     * Add a value with its timestamp.
     *
     * @param timestamp The timestamp of the value
     * @param value The value to store
     * @throws IllegalArgumentException If a value already exists at the given timestamp
     */
    fun add(timestamp: Instant, value: T)

    /**
     * Add a value with a guaranteed unique timestamp.
     * If the timestamp already exists, adds a random microsecond offset.
     *
     * @param timestamp The desired timestamp
     * @param value The value to store
     * @param maxOffsetMicroseconds Maximum random offset to add (default: 1 second)
     * @return The actual timestamp used (may be different from input if offset was added)
     */
    fun addUniqueTimestamp(timestamp: Instant, value: T, maxOffsetMicroseconds: Int = 1_000_000): Instant

    /**
     * Get all values within a time range.
     *
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return List of values within the specified time range
     */
    fun getRange(startTime: Instant, endTime: Instant): List<T>

    /**
     * Get all values within the last duration.
     *
     * @param duration Duration to look back
     * @return List of values within the specified duration
     */
    fun getDuration(duration: Duration): List<T>

    /**
     * Clear all stored values.
     */
    fun clear()

    /**
     * Get all stored values.
     *
     * @return List of all stored values
     */
    fun getAll(): List<T>

    /**
     * Get all stored timestamps.
     *
     * @return List of all stored timestamps
     */
    fun getTimestamps(): List<Instant>

    /**
     * Get the value at a specific timestamp.
     *
     * @param timestamp The timestamp to look up
     * @return The value at the specified timestamp, or null if not found
     */
    fun getValueAt(timestamp: Instant): T?

    /**
     * Remove a value at a specific timestamp.
     *
     * @param timestamp The timestamp of the value to remove
     * @return true if the value was removed, false if not found
     */
    fun remove(timestamp: Instant): Boolean

    /**
     * Get the number of stored values.
     *
     * @return Number of stored values
     */
    fun size(): Int

    /**
     * Check if the storage is empty.
     *
     * @return true if the storage is empty, false otherwise
     */
    fun isEmpty(): Boolean
} 
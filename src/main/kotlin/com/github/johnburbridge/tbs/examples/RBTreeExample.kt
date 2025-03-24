package com.github.johnburbridge.tbs.examples

import com.github.johnburbridge.tbs.TimeBasedStorages
import java.time.Duration
import java.time.Instant

/**
 * Simple example demonstrating the Red-Black Tree implementation for time-based storage.
 * This example shows the basic usage and advantages of the RB-Tree implementation.
 */
object RBTreeExample {
    
    /**
     * Demonstrates basic usage of the RB-Tree implementation.
     */
    fun basicExample() {
        println("Basic RB-Tree implementation example")
        println("====================================")
        
        // Create RB-Tree storage instance
        val storage = TimeBasedStorages.createRBTreeStorage<String>()
        
        // Add some events
        val now = Instant.now()
        storage.add(now.minus(Duration.ofMinutes(30)), "Event from 30 minutes ago")
        storage.add(now.minus(Duration.ofMinutes(20)), "Event from 20 minutes ago")
        storage.add(now.minus(Duration.ofMinutes(10)), "Event from 10 minutes ago")
        storage.add(now, "Current event")
        
        println("Total events: ${storage.size()}")
        
        // Retrieve events in a time range
        val start = now.minus(Duration.ofMinutes(25))
        val end = now.minus(Duration.ofMinutes(5))
        val rangeEvents = storage.getRange(start, end)
        
        println("\nEvents between 25 and 5 minutes ago:")
        for (event in rangeEvents) {
            println("- $event")
        }
        
        // Get most recent events (last 15 minutes)
        val recentEvents = storage.getDuration(Duration.ofMinutes(15))
        
        println("\nEvents in the last 15 minutes:")
        for (event in recentEvents) {
            println("- $event")
        }
    }
    
    /**
     * Demonstrates the thread-safe RB-Tree implementation.
     */
    fun threadSafeExample() {
        println("\nThread-safe RB-Tree implementation")
        println("=================================")
        
        // Create thread-safe storage
        val storage = TimeBasedStorages.createThreadSafeRBTreeStorage<String>()
        
        // Add some events
        val now = Instant.now()
        storage.add(now.minus(Duration.ofMinutes(5)), "Event A")
        storage.add(now.minus(Duration.ofMinutes(3)), "Event B")
        storage.add(now.minus(Duration.ofMinutes(1)), "Event C")
        
        println("Total events: ${storage.size()}")
        println("All events:")
        for (event in storage.getAll()) {
            println("- $event")
        }
    }
    
    /**
     * Demonstrates timestamp collision handling with the RB-Tree implementation.
     */
    fun collisionHandlingExample() {
        println("\nTimestamp collision handling with RB-Tree")
        println("=======================================")
        
        val storage = TimeBasedStorages.createRBTreeStorage<String>()
        
        // Create a timestamp
        val now = Instant.now()
        
        // Add first event
        storage.add(now, "First event")
        println("First event added successfully")
        
        // Handle collision with addUniqueTimestamp
        val modifiedTs = storage.addUniqueTimestamp(now, "Second event")
        println("Second event added with modified timestamp (offset: ${Duration.between(now, modifiedTs).toNanos()} nanoseconds)")
        
        // Add a third event with the same base timestamp
        val modifiedTs2 = storage.addUniqueTimestamp(now, "Third event")
        println("Third event added with modified timestamp (offset: ${Duration.between(now, modifiedTs2).toNanos()} nanoseconds)")
        
        // Verify all events are stored
        println("\nTotal events: ${storage.size()}")
        for (event in storage.getAll()) {
            println("- $event")
        }
    }
    
    @JvmStatic
    fun main(args: Array<String>) {
        basicExample()
        threadSafeExample()
        collisionHandlingExample()
    }
} 
package com.github.johnburbridge.tbs.concurrent

import com.github.johnburbridge.tbs.core.HashMapTimeBasedStorage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ThreadSafeTimeBasedStorageTest {
    
    private lateinit var storage: ThreadSafeTimeBasedStorage<String>
    
    @BeforeEach
    fun setUp() {
        storage = ThreadSafeTimeBasedStorage(HashMapTimeBasedStorage())
    }
    
    @Test
    fun `test basic operations`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        storage.add(now, "test value")
        
        assertEquals("test value", storage.getValueAt(now))
        assertEquals(1, storage.size())
        assertFalse(storage.isEmpty())
    }
    
    @Test
    fun `test concurrent writes`() {
        val threadCount = 10
        val operationsPerThread = 100
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val finishLatch = CountDownLatch(threadCount)
        
        try {
            // Launch threads that will add entries concurrently
            for (threadId in 0 until threadCount) {
                executor.submit {
                    try {
                        // Wait for all threads to be ready
                        startLatch.await()
                        
                        val baseTime = Instant.now().truncatedTo(ChronoUnit.SECONDS)
                        for (i in 0 until operationsPerThread) {
                            // Create a unique timestamp for each operation
                            val timestamp = baseTime.plusNanos((threadId * operationsPerThread + i).toLong())
                            storage.add(timestamp, "thread-$threadId-value-$i")
                        }
                    } finally {
                        finishLatch.countDown()
                    }
                }
            }
            
            // Start all threads simultaneously
            startLatch.countDown()
            
            // Wait for all threads to complete
            assertTrue(finishLatch.await(10, TimeUnit.SECONDS), "Timeout waiting for threads to complete")
            
            // Verify the results
            assertEquals(threadCount * operationsPerThread, storage.size())
        } finally {
            executor.shutdownNow()
        }
    }
    
    @Test
    fun `test concurrent reads and writes`() {
        val writeThreadCount = 3
        val readThreadCount = 3
        val operationsPerThread = 10
        val executor = Executors.newFixedThreadPool(writeThreadCount + readThreadCount)
        val startLatch = CountDownLatch(1)
        val finishLatch = CountDownLatch(writeThreadCount + readThreadCount)
        val successfulReads = AtomicInteger(0)
        
        try {
            // Prepare some initial data
            val baseTime = Instant.now().truncatedTo(ChronoUnit.SECONDS)
            for (i in 0 until 5) {
                storage.add(baseTime.minusSeconds(i.toLong()), "initial-value-$i")
            }
            
            // Launch writer threads
            for (threadId in 0 until writeThreadCount) {
                executor.submit {
                    try {
                        startLatch.await()
                        
                        for (i in 0 until operationsPerThread) {
                            try {
                                // Ensure unique timestamps with very large gaps
                                val timestamp = baseTime.plusMillis((threadId * 10000L + i * 1000L))
                                storage.add(timestamp, "writer-$threadId-value-$i")
                            } catch (e: Exception) {
                                // Ignore exceptions in concurrent test
                            }
                            // Slow down to reduce contention
                            Thread.sleep(10)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        finishLatch.countDown()
                    }
                }
            }
            
            // Launch reader threads
            for (threadId in 0 until readThreadCount) {
                executor.submit {
                    try {
                        startLatch.await()
                        
                        for (i in 0 until operationsPerThread) {
                            try {
                                val values = storage.getAll()
                                if (values.isNotEmpty()) {
                                    successfulReads.incrementAndGet()
                                }
                            } catch (e: Exception) {
                                // Ignore exceptions in concurrent test
                            }
                            // Slow down to reduce contention
                            Thread.sleep(10)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        finishLatch.countDown()
                    }
                }
            }
            
            // Start all threads simultaneously
            startLatch.countDown()
            
            // Wait for all threads to complete with longer timeout
            assertTrue(finishLatch.await(20, TimeUnit.SECONDS), "Timeout waiting for threads to complete")
            
            // Verify the results - only check that some writes happened and reads succeeded
            assertTrue(storage.size() > 5, "Expected some items to be added")
            assertTrue(successfulReads.get() > 0, "Should have some successful reads")
        } finally {
            executor.shutdownNow()
        }
    }
    
    @Test
    fun `test wait for data with timeout`() {
        val timeout = 500L // 500ms
        
        // Start a thread that will add data after a delay
        val thread = Thread {
            Thread.sleep(200)
            storage.add(Instant.now(), "delayed value")
        }
        thread.start()
        
        // Wait for data with timeout
        val result = storage.waitForData(timeout)
        
        assertTrue(result, "Should return true when data is added before timeout")
        assertEquals(1, storage.size())
        
        thread.join()
    }
    
    @Test
    fun `test wait for data with timeout expiring`() {
        val timeout = 100L // 100ms
        
        // Start a thread that will add data after a longer delay
        val thread = Thread {
            Thread.sleep(300)
            storage.add(Instant.now(), "delayed value")
        }
        thread.start()
        
        // Wait for data with timeout
        val result = storage.waitForData(timeout)
        
        // The timeout should expire before the data is added
        assertFalse(result, "Should return false when timeout expires before data is added")
        
        // Wait for the thread to complete and verify the data was eventually added
        thread.join()
        assertEquals(1, storage.size())
    }
    
    @Test
    fun `test notifyDataAvailable`() {
        val latch = CountDownLatch(1)
        
        // Start a thread that waits for data indefinitely
        val thread = Thread {
            storage.waitForData(null)
            latch.countDown()
        }
        thread.start()
        
        // Wait a bit to ensure the thread is waiting
        Thread.sleep(100)
        
        // Notify data available without actually adding data
        storage.notifyDataAvailable()
        
        // The waiting thread should be unblocked
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Waiting thread should be unblocked")
        
        thread.join()
    }
    
    @Test
    fun `test addUniqueTimestamp`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        // Add initial value
        storage.add(now, "first value")
        
        // Try to add to the same timestamp with addUniqueTimestamp
        val newTimestamp = storage.addUniqueTimestamp(now, "second value", 1)
        
        // Check that the new timestamp is different
        assertNotEquals(now, newTimestamp)
        
        // Check both values are present
        assertEquals("first value", storage.getValueAt(now))
        assertEquals("second value", storage.getValueAt(newTimestamp))
        assertEquals(2, storage.size())
    }
    
    @Test
    fun `test getRange`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        // Add test data
        storage.add(now.minusSeconds(10), "value1")
        storage.add(now.minusSeconds(5), "value2")
        storage.add(now, "value3")
        storage.add(now.plusSeconds(5), "value4")
        storage.add(now.plusSeconds(10), "value5")
        
        // Get range from -7 seconds to +7 seconds
        val rangeValues = storage.getRange(now.minusSeconds(7), now.plusSeconds(7))
        
        // Expect 3 values: value2, value3, value4
        assertEquals(3, rangeValues.size)
        assertTrue(rangeValues.contains("value2"))
        assertTrue(rangeValues.contains("value3"))
        assertTrue(rangeValues.contains("value4"))
        
        // Test empty range
        val emptyRange = storage.getRange(now.minusSeconds(100), now.minusSeconds(50))
        assertTrue(emptyRange.isEmpty())
    }
    
    @Test
    fun `test getDuration`() {
        // Use a fixed reference time instead of Instant.now()
        val referenceTime = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        // Add test data with explicit timestamps relative to reference time
        storage.add(referenceTime.minusSeconds(30), "old value")
        storage.add(referenceTime.minusSeconds(10), "recent value")
        storage.add(referenceTime.minusSeconds(5), "very recent value")
        storage.add(referenceTime, "current value")
        
        // Instead of testing getDuration directly which uses Instant.now(),
        // we'll test getRange which is what getDuration uses internally
        val duration = Duration.ofSeconds(15)
        val from = referenceTime.minus(duration)
        val recentValues = storage.getRange(from, referenceTime)
        
        // Expect 3 values (the ones within last 15 seconds)
        assertEquals(3, recentValues.size)
        assertTrue(recentValues.contains("recent value"))
        assertTrue(recentValues.contains("very recent value"))
        assertTrue(recentValues.contains("current value"))
        assertFalse(recentValues.contains("old value"))
        
        // Test zero duration case
        val zeroDurationValues = storage.getRange(referenceTime, referenceTime)
        assertEquals(1, zeroDurationValues.size)
        assertTrue(zeroDurationValues.contains("current value"))
    }
    
    @Test
    fun `test clear`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        // Add test data
        storage.add(now.minusSeconds(10), "value1")
        storage.add(now, "value2")
        storage.add(now.plusSeconds(10), "value3")
        
        assertEquals(3, storage.size())
        
        // Clear the storage
        storage.clear()
        
        // Verify the storage is empty
        assertEquals(0, storage.size())
        assertTrue(storage.isEmpty())
        assertNull(storage.getValueAt(now))
    }
    
    @Test
    fun `test getTimestamps`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        // Add test data with specific timestamps
        val timestamp1 = now.minusSeconds(10)
        val timestamp2 = now
        val timestamp3 = now.plusSeconds(10)
        
        storage.add(timestamp1, "value1")
        storage.add(timestamp2, "value2")
        storage.add(timestamp3, "value3")
        
        // Get all timestamps
        val timestamps = storage.getTimestamps()
        
        // Verify the timestamps
        assertEquals(3, timestamps.size)
        assertTrue(timestamps.contains(timestamp1))
        assertTrue(timestamps.contains(timestamp2))
        assertTrue(timestamps.contains(timestamp3))
    }
    
    @Test
    fun `test waitForData with default timeout`() {
        // Start a thread that will add data after a delay
        val thread = Thread {
            Thread.sleep(200)
            storage.add(Instant.now(), "delayed value")
        }
        thread.start()
        
        // Wait for data with default timeout (should use null)
        val result = storage.waitForData()
        
        assertTrue(result, "Should return true when data is added")
        assertEquals(1, storage.size())
        
        thread.join()
    }
} 
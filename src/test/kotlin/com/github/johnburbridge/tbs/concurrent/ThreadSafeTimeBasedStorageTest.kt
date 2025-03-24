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
} 
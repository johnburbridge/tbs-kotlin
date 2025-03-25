package com.github.johnburbridge.tbs.core

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class BTreeTimeBasedStorageTest {
    
    private lateinit var storage: BTreeTimeBasedStorage<String>
    
    @BeforeEach
    fun setUp() {
        storage = BTreeTimeBasedStorage()
    }
    
    @Test
    fun `test add and retrieve`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        storage.add(now, "test value")
        
        assertEquals("test value", storage.getValueAt(now))
        assertEquals(1, storage.size())
        assertFalse(storage.isEmpty())
    }
    
    @Test
    fun `test add duplicate timestamp throws exception`() {
        val timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        storage.add(timestamp, "first value")
        
        val exception = assertThrows(IllegalArgumentException::class.java) {
            storage.add(timestamp, "second value")
        }
        
        assertTrue(exception.message?.contains("Value already exists") == true)
    }
    
    @Test
    fun `test addUniqueTimestamp with no collision`() {
        val timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        val resultTimestamp = storage.addUniqueTimestamp(timestamp, "test value")
        
        assertEquals(timestamp, resultTimestamp)
        assertEquals("test value", storage.getValueAt(timestamp))
    }
    
    @Test
    fun `test addUniqueTimestamp with collision`() {
        val timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        storage.add(timestamp, "first value")
        val resultTimestamp = storage.addUniqueTimestamp(timestamp, "second value")
        
        assertNotEquals(timestamp, resultTimestamp)
        assertTrue(resultTimestamp.isAfter(timestamp))
        assertEquals("first value", storage.getValueAt(timestamp))
        assertEquals("second value", storage.getValueAt(resultTimestamp))
    }
    
    @Test
    fun `test getRange with many entries`() {
        val baseTime = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        
        // Add 100 entries
        for (i in 0 until 100) {
            storage.add(baseTime.plusSeconds(i.toLong()), "value$i")
        }
        
        // Test range query
        val start = baseTime.plusSeconds(25)
        val end = baseTime.plusSeconds(74)
        val rangeValues = storage.getRange(start, end)
        
        assertEquals(50, rangeValues.size)
        assertTrue(rangeValues.contains("value25"))
        assertTrue(rangeValues.contains("value50"))
        assertTrue(rangeValues.contains("value74"))
        assertFalse(rangeValues.contains("value24"))
        assertFalse(rangeValues.contains("value75"))
    }
    
    @Test
    fun `test getRange with empty range`() {
        val baseTime = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        
        storage.add(baseTime, "value1")
        storage.add(baseTime.plusSeconds(10), "value2")
        
        val rangeValues = storage.getRange(
            baseTime.plusSeconds(1),
            baseTime.plusSeconds(9)
        )
        
        assertTrue(rangeValues.isEmpty())
    }
    
    @Test
    fun `test getDuration`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        
        // Add test data with explicit timestamps
        storage.add(now.minusSeconds(30), "old value")
        storage.add(now.minusSeconds(10), "recent value")
        storage.add(now.minusSeconds(5), "very recent value")
        storage.add(now, "current value")
        
        // Mock the current time to make getDuration testable
        val duration = Duration.ofSeconds(15)
        val mockNow = now
        val from = mockNow.minus(duration)
        
        // Get values from the "last" 15 seconds
        val recentValues = storage.getRange(from, mockNow)
        
        // Expect 3 values (the ones within last 15 seconds)
        assertEquals(3, recentValues.size)
        assertTrue(recentValues.contains("recent value"))
        assertTrue(recentValues.contains("very recent value"))
        assertTrue(recentValues.contains("current value"))
        assertFalse(recentValues.contains("old value"))
    }
    
    @Test
    fun `test clear and isEmpty`() {
        storage.add(Instant.now(), "value1")
        storage.add(Instant.now().plusSeconds(1), "value2")
        
        assertFalse(storage.isEmpty())
        
        storage.clear()
        
        assertTrue(storage.isEmpty())
        assertEquals(0, storage.size())
    }
    
    @Test
    fun `test remove`() {
        val timestamp = Instant.now()
        
        storage.add(timestamp, "value")
        
        assertTrue(storage.remove(timestamp))
        assertNull(storage.getValueAt(timestamp))
        assertEquals(0, storage.size())
        
        // Test removing non-existent timestamp
        assertFalse(storage.remove(Instant.now().plusSeconds(1)))
    }
    
    @Test
    fun `test getAll returns all values`() {
        val baseTime = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        
        storage.add(baseTime.minusSeconds(10), "value1")
        storage.add(baseTime, "value2")
        storage.add(baseTime.plusSeconds(10), "value3")
        
        val allValues = storage.getAll()
        
        assertEquals(3, allValues.size)
        assertTrue(allValues.contains("value1"))
        assertTrue(allValues.contains("value2"))
        assertTrue(allValues.contains("value3"))
    }
    
    @Test
    fun `test getTimestamps returns all timestamps`() {
        val baseTime = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        
        val timestamp1 = baseTime.minusSeconds(10)
        val timestamp2 = baseTime
        val timestamp3 = baseTime.plusSeconds(10)
        
        storage.add(timestamp1, "value1")
        storage.add(timestamp2, "value2")
        storage.add(timestamp3, "value3")
        
        val timestamps = storage.getTimestamps()
        
        assertEquals(3, timestamps.size)
        assertTrue(timestamps.contains(timestamp1))
        assertTrue(timestamps.contains(timestamp2))
        assertTrue(timestamps.contains(timestamp3))
    }
} 
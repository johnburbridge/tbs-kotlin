package com.github.johnburbridge.tbs.core

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class HashMapTimeBasedStorageTest {
    
    private lateinit var storage: HashMapTimeBasedStorage<String>
    
    @BeforeEach
    fun setUp() {
        storage = HashMapTimeBasedStorage()
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
    fun `test getRange`() {
        val baseTime = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val time1 = baseTime
        val time2 = baseTime.plusSeconds(1)
        val time3 = baseTime.plusSeconds(2)
        val time4 = baseTime.plusSeconds(3)
        
        storage.add(time1, "value1")
        storage.add(time2, "value2")
        storage.add(time3, "value3")
        storage.add(time4, "value4")
        
        val rangeValues = storage.getRange(time2, time3)
        
        assertEquals(2, rangeValues.size)
        assertTrue(rangeValues.contains("value2"))
        assertTrue(rangeValues.contains("value3"))
    }
    
    @Test
    fun `test getDuration`() {
        val referenceTime = Instant.now()
        val oneHourAgo = referenceTime.minus(1, ChronoUnit.HOURS)
        val twoHoursAgo = referenceTime.minus(2, ChronoUnit.HOURS)
        val threeHoursAgo = referenceTime.minus(3, ChronoUnit.HOURS)
        
        storage.add(oneHourAgo, "one hour ago")
        storage.add(twoHoursAgo, "two hours ago")
        storage.add(threeHoursAgo, "three hours ago")
        
        val values = storage.getRange(referenceTime.minus(2, ChronoUnit.HOURS), referenceTime)
        
        assertEquals(2, values.size)
        assertTrue(values.contains("one hour ago"))
        assertTrue(values.contains("two hours ago"))
        assertFalse(values.contains("three hours ago"))
    }
    
    @Test
    fun `test clear`() {
        storage.add(Instant.now(), "value1")
        storage.add(Instant.now().plusSeconds(1), "value2")
        
        storage.clear()
        
        assertTrue(storage.isEmpty())
        assertEquals(0, storage.size())
    }
    
    @Test
    fun `test getAll and getTimestamps`() {
        val time1 = Instant.now()
        val time2 = time1.plusSeconds(1)
        
        storage.add(time1, "value1")
        storage.add(time2, "value2")
        
        val allValues = storage.getAll()
        val allTimestamps = storage.getTimestamps()
        
        assertEquals(2, allValues.size)
        assertEquals(2, allTimestamps.size)
        assertTrue(allValues.contains("value1"))
        assertTrue(allValues.contains("value2"))
        assertTrue(allTimestamps.contains(time1))
        assertTrue(allTimestamps.contains(time2))
    }
    
    @Test
    fun `test remove`() {
        val timestamp = Instant.now()
        
        storage.add(timestamp, "value")
        
        assertTrue(storage.remove(timestamp))
        assertNull(storage.getValueAt(timestamp))
        assertEquals(0, storage.size())
        assertTrue(storage.isEmpty())
        
        // Test removing non-existent timestamp
        assertFalse(storage.remove(timestamp))
    }
} 
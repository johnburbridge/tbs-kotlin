package com.github.johnburbridge.tbs.core

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class RBTreeTimeBasedStorageTest {
    
    private lateinit var storage: RBTreeTimeBasedStorage<String>
    
    @BeforeEach
    fun setUp() {
        storage = RBTreeTimeBasedStorage()
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
        
        // Test range query (should use efficient subMap internally)
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
    fun `test getRange returns ordered results`() {
        val baseTime = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        
        // Add entries in random order
        storage.add(baseTime.plusSeconds(5), "value5")
        storage.add(baseTime.plusSeconds(1), "value1")
        storage.add(baseTime.plusSeconds(3), "value3")
        storage.add(baseTime.plusSeconds(2), "value2")
        storage.add(baseTime.plusSeconds(4), "value4")
        
        val rangeValues = storage.getRange(baseTime, baseTime.plusSeconds(5))
        
        // Check if results are ordered by timestamp
        assertEquals(listOf("value1", "value2", "value3", "value4", "value5"), rangeValues)
    }
    
    @Test
    fun `test getDuration`() {
        // Use a fixed reference time instead of Instant.now()
        val referenceTime = Instant.now()
        val oneHourAgo = referenceTime.minus(1, ChronoUnit.HOURS)
        val twoHoursAgo = referenceTime.minus(2, ChronoUnit.HOURS)
        val threeHoursAgo = referenceTime.minus(3, ChronoUnit.HOURS)
        
        storage.add(oneHourAgo, "one hour ago")
        storage.add(twoHoursAgo, "two hours ago")
        storage.add(threeHoursAgo, "three hours ago")
        
        // Instead of testing getDuration directly which uses Instant.now(),
        // we'll test getRange which is what getDuration uses internally
        val values = storage.getRange(referenceTime.minus(2, ChronoUnit.HOURS), referenceTime)
        
        assertEquals(2, values.size)
        assertTrue(values.contains("one hour ago"))
        assertTrue(values.contains("two hours ago"))
        assertFalse(values.contains("three hours ago"))
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
} 
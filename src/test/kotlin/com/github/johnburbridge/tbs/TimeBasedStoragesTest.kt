package com.github.johnburbridge.tbs

import com.github.johnburbridge.tbs.concurrent.ThreadSafeTimeBasedStorage
import com.github.johnburbridge.tbs.core.BTreeTimeBasedStorage
import com.github.johnburbridge.tbs.core.HashMapTimeBasedStorage
import com.github.johnburbridge.tbs.core.RBTreeTimeBasedStorage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class TimeBasedStoragesTest {
    
    @Test
    fun `test create HashMap storage`() {
        val storage = TimeBasedStorages.createHashMapStorage<String>()
        
        assertNotNull(storage)
        assertTrue(storage is HashMapTimeBasedStorage)
        assertTrue(storage.isEmpty())
    }
    
    @Test
    fun `test create RB tree storage`() {
        val storage = TimeBasedStorages.createRBTreeStorage<String>()
        
        assertNotNull(storage)
        assertTrue(storage is RBTreeTimeBasedStorage)
        assertTrue(storage.isEmpty())
    }
    
    @Test
    fun `test create B-Tree storage`() {
        val storage = TimeBasedStorages.createBTreeStorage<String>()
        
        assertNotNull(storage)
        assertTrue(storage is BTreeTimeBasedStorage)
        assertTrue(storage.isEmpty())
    }
    
    @Test
    fun `test create thread-safe HashMap storage`() {
        val storage = TimeBasedStorages.createThreadSafeHashMapStorage<String>()
        
        assertNotNull(storage)
        assertEquals(ThreadSafeTimeBasedStorage::class.java, storage::class.java)
        assertTrue(storage.isEmpty())
    }
    
    @Test
    fun `test create thread-safe RB tree storage`() {
        val storage = TimeBasedStorages.createThreadSafeRBTreeStorage<String>()
        
        assertNotNull(storage)
        assertEquals(ThreadSafeTimeBasedStorage::class.java, storage::class.java)
        assertTrue(storage.isEmpty())
    }
    
    @Test
    fun `test create thread-safe B-Tree storage`() {
        val storage = TimeBasedStorages.createThreadSafeBTreeStorage<String>()
        
        assertNotNull(storage)
        assertEquals(ThreadSafeTimeBasedStorage::class.java, storage::class.java)
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `test deprecated dictionary methods redirect to HashMap methods`() {
        // Check that the deprecated dictionary methods work and use the HashMap implementations
        val dictStorage = TimeBasedStorages.createDictionaryStorage<String>()
        val dictStorageClass = dictStorage::class.java
        assertEquals(HashMapTimeBasedStorage::class.java, dictStorageClass)
        
        val threadSafeDictStorage = TimeBasedStorages.createThreadSafeDictionaryStorage<String>()
        assertEquals(ThreadSafeTimeBasedStorage::class.java, threadSafeDictStorage::class.java)
    }
    
    @Test
    fun `test basic operations on factory-created storage`() {
        // Test all factory methods to ensure they create working instances
        val storages = listOf(
            TimeBasedStorages.createHashMapStorage<String>(),
            TimeBasedStorages.createRBTreeStorage<String>(),
            TimeBasedStorages.createBTreeStorage<String>(),
            TimeBasedStorages.createThreadSafeHashMapStorage<String>(),
            TimeBasedStorages.createThreadSafeRBTreeStorage<String>(),
            TimeBasedStorages.createThreadSafeBTreeStorage<String>()
        )
        
        for (storage in storages) {
            val now = Instant.now()
            
            // Test adding and retrieving
            storage.add(now, "test value")
            assertEquals("test value", storage.getValueAt(now))
            assertEquals(1, storage.size())
            
            // Test remove
            assertTrue(storage.remove(now))
            assertNull(storage.getValueAt(now))
            assertEquals(0, storage.size())
            assertTrue(storage.isEmpty())
        }
    }
} 
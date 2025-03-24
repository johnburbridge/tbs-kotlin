package com.github.johnburbridge.tbs

import com.github.johnburbridge.tbs.concurrent.ThreadSafeTimeBasedStorage
import com.github.johnburbridge.tbs.core.DictionaryTimeBasedStorage
import com.github.johnburbridge.tbs.core.RBTreeTimeBasedStorage

/**
 * Utility factory methods for creating TimeBasedStorage instances.
 */
object TimeBasedStorages {
    
    /**
     * Creates a new Dictionary-based storage implementation.
     * This implementation uses a HashMap and is suitable for small to medium datasets.
     *
     * @return A new DictionaryTimeBasedStorage instance
     */
    fun <T> createDictionaryStorage(): TimeBasedStorage<T> {
        return DictionaryTimeBasedStorage()
    }
    
    /**
     * Creates a new Red-Black Tree based storage implementation.
     * This implementation offers balanced performance for both insertions and range queries.
     *
     * @return A new RBTreeTimeBasedStorage instance
     */
    fun <T> createRBTreeStorage(): TimeBasedStorage<T> {
        return RBTreeTimeBasedStorage()
    }
    
    /**
     * Creates a new thread-safe Dictionary-based storage implementation.
     * This implementation is safe to use from multiple threads.
     *
     * @return A new thread-safe DictionaryTimeBasedStorage instance
     */
    fun <T> createThreadSafeDictionaryStorage(): ThreadSafeTimeBasedStorage<T> {
        return ThreadSafeTimeBasedStorage(DictionaryTimeBasedStorage())
    }
    
    /**
     * Creates a new thread-safe Red-Black Tree based storage implementation.
     * This implementation is safe to use from multiple threads and offers
     * balanced performance for both insertions and range queries.
     *
     * @return A new thread-safe RBTreeTimeBasedStorage instance
     */
    fun <T> createThreadSafeRBTreeStorage(): ThreadSafeTimeBasedStorage<T> {
        return ThreadSafeTimeBasedStorage(RBTreeTimeBasedStorage())
    }
} 
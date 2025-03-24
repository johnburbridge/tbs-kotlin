package com.github.johnburbridge.tbs

import com.github.johnburbridge.tbs.concurrent.ThreadSafeTimeBasedStorage
import com.github.johnburbridge.tbs.core.BTreeTimeBasedStorage
import com.github.johnburbridge.tbs.core.HashMapTimeBasedStorage
import com.github.johnburbridge.tbs.core.RBTreeTimeBasedStorage

/**
 * Utility factory methods for creating TimeBasedStorage instances.
 */
object TimeBasedStorages {
    
    /**
     * Creates a new HashMap-based storage implementation.
     * This implementation uses a HashMap and is suitable for small to medium datasets.
     *
     * @return A new HashMapTimeBasedStorage instance
     */
    fun <T> createHashMapStorage(): TimeBasedStorage<T> {
        return HashMapTimeBasedStorage()
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
     * Creates a new B-Tree based storage implementation.
     * This implementation offers good memory locality and is efficient for range queries.
     *
     * @return A new BTreeTimeBasedStorage instance
     */
    fun <T> createBTreeStorage(): TimeBasedStorage<T> {
        return BTreeTimeBasedStorage()
    }
    
    /**
     * Creates a new thread-safe HashMap-based storage implementation.
     * This implementation is safe to use from multiple threads.
     *
     * @return A new thread-safe HashMapTimeBasedStorage instance
     */
    fun <T> createThreadSafeHashMapStorage(): ThreadSafeTimeBasedStorage<T> {
        return ThreadSafeTimeBasedStorage(HashMapTimeBasedStorage())
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
    
    /**
     * Creates a new thread-safe B-Tree based storage implementation.
     * This implementation is safe to use from multiple threads and offers
     * good memory locality and efficient range queries.
     *
     * @return A new thread-safe BTreeTimeBasedStorage instance
     */
    fun <T> createThreadSafeBTreeStorage(): ThreadSafeTimeBasedStorage<T> {
        return ThreadSafeTimeBasedStorage(BTreeTimeBasedStorage())
    }

    /**
     * Creates a new HashMap-based storage implementation.
     * @deprecated Use createHashMapStorage() instead
     */
    @Deprecated("Use createHashMapStorage() instead", ReplaceWith("createHashMapStorage<T>()"))
    fun <T> createDictionaryStorage(): TimeBasedStorage<T> {
        return createHashMapStorage()
    }

    /**
     * Creates a new thread-safe HashMap-based storage implementation.
     * @deprecated Use createThreadSafeHashMapStorage() instead
     */
    @Deprecated("Use createThreadSafeHashMapStorage() instead", ReplaceWith("createThreadSafeHashMapStorage<T>()"))
    fun <T> createThreadSafeDictionaryStorage(): ThreadSafeTimeBasedStorage<T> {
        return createThreadSafeHashMapStorage()
    }
} 
package com.github.johnburbridge.tbs.concurrent

import com.github.johnburbridge.tbs.TimeBasedStorage
import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe wrapper for any TimeBasedStorage implementation.
 * This class provides thread-safe access to the underlying storage using read-write locks.
 *
 * @param delegate The underlying storage implementation to delegate to
 */
class ThreadSafeTimeBasedStorage<T>(
    private val delegate: TimeBasedStorage<T>
) : TimeBasedStorage<T> {
    
    private val lock = ReentrantReadWriteLock()
    private val condition = lock.writeLock().newCondition()
    
    override fun add(timestamp: Instant, value: T) {
        lock.write {
            delegate.add(timestamp, value)
            // Notify any waiting threads that new data is available
            condition.signalAll()
        }
    }
    
    override fun addUniqueTimestamp(timestamp: Instant, value: T, maxOffsetMicroseconds: Int): Instant {
        return lock.write {
            val result = delegate.addUniqueTimestamp(timestamp, value, maxOffsetMicroseconds)
            // Notify any waiting threads that new data is available
            condition.signalAll()
            result
        }
    }
    
    override fun getRange(startTime: Instant, endTime: Instant): List<T> {
        return lock.read {
            delegate.getRange(startTime, endTime)
        }
    }
    
    override fun getDuration(duration: Duration): List<T> {
        return lock.read {
            delegate.getDuration(duration)
        }
    }
    
    override fun clear() {
        lock.write {
            delegate.clear()
        }
    }
    
    override fun getAll(): List<T> {
        return lock.read {
            delegate.getAll()
        }
    }
    
    override fun getTimestamps(): List<Instant> {
        return lock.read {
            delegate.getTimestamps()
        }
    }
    
    override fun getValueAt(timestamp: Instant): T? {
        return lock.read {
            delegate.getValueAt(timestamp)
        }
    }
    
    override fun remove(timestamp: Instant): Boolean {
        return lock.write {
            delegate.remove(timestamp)
        }
    }
    
    override fun size(): Int {
        return lock.read {
            delegate.size()
        }
    }
    
    override fun isEmpty(): Boolean {
        return lock.read {
            delegate.isEmpty()
        }
    }
    
    /**
     * Wait for new data to be added to the storage.
     *
     * @param timeout Maximum time to wait in milliseconds. If null, wait indefinitely.
     * @return true if data was received, false if timeout occurred
     */
    fun waitForData(timeout: Long? = null): Boolean {
        return lock.write {
            if (timeout != null) {
                condition.await(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
            } else {
                condition.await()
                true
            }
        }
    }
    
    /**
     * Notify all waiting threads that data is available.
     */
    fun notifyDataAvailable() {
        lock.write {
            condition.signalAll()
        }
    }
} 
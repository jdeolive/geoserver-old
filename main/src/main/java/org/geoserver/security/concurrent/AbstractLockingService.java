/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract base class for locking support.
 * 
 * @author christian
 *
 */
public abstract class AbstractLockingService {

    protected final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    protected final Lock readLock = readWriteLock.readLock();
    protected final Lock writeLock = readWriteLock.writeLock();

    /**
     *  get a read lock
     */
    protected void  readLock() {
        readLock.lock();
    }

    /**
     *  free read lock
     */
    protected void  readUnLock() {
        readLock.unlock();
    }

    /**
     *  get a write lock
     */
    protected void  writeLock() {
        writeLock.lock();
    }

    /**
     *  free write lock
     */
    protected void  writeUnLock() {
        writeLock.unlock();
    }

    
}

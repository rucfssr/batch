package com.batch.model;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.batch.exception.BatchAlreadyCommitted;

/**
 * Model class for Batch. It uses ReadWriteLock to ensure consistency.
 * 
 * @author faizanhussain
 *
 */
public class Batch {

	private static final Logger log = LoggerFactory.getLogger(Batch.class);

	private final long id;
	private final ConcurrentMap<Long, Price> pricesMap = new ConcurrentHashMap<>();
	private boolean isValidBatch = true;
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

	public Batch(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	/**
	 * Acquire the lock, For each price in prices compute the new price and release
	 * lock.
	 * 
	 * @param prices
	 */
	public void upload(List<Price> prices) {
		log.info("Starting batch {}", id);
		try {
			rwLock.readLock().lock();
			if (!isValidBatch) {
				throw new BatchAlreadyCommitted(id);
			}
			prices.forEach(price -> pricesMap.compute(price.getId(), new PriceFinder(price)));
		} finally {
			rwLock.readLock().unlock();
		}
		log.info("Ending batch {}", id);
	}

	/**
	 * Acquire the lock, close the batch and return the latest prices and release
	 * lock.
	 * 
	 * @return
	 */
	public Collection<Price> closeAndRefresh() {
		log.info("Closing batch {}", id);
		try {
			rwLock.writeLock().lock();
			isValidBatch = false;
			return pricesMap.values();
		} finally {
			rwLock.writeLock().unlock();
			log.info("Batch closed for {}", id);
		}
	}

}

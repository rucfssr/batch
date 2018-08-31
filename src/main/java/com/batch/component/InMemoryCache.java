package com.batch.component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.batch.exception.BatchNotFoundException;
import com.batch.model.Batch;
import com.batch.model.Price;

/**
 * In memory cache. This class ensures that batches which are in processing
 * shouldn't be available before commit.
 * 
 * @author faizanhussain
 *
 */
@Component
public class InMemoryCache {

	private static final Logger log = LoggerFactory.getLogger(InMemoryCache.class);

	private final Cache<Long, Batch> currentBatches;
	private final AtomicLong id = new AtomicLong(1);

	public InMemoryCache() {
		log.info("Intializing cache.");

		final CacheConfiguration<Long, Batch> configuration = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Long.class, Batch.class, ResourcePoolsBuilder.heap(10000)).build();

		final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("batches", configuration).build();

		cacheManager.init();
		currentBatches = cacheManager.getCache("batches", Long.class, Batch.class);
		log.info("Cache created.");
	}

	public Batch create() {
		log.info("Creating new batch");
		final Batch batch = new Batch(id.getAndIncrement());
		currentBatches.put(batch.getId(), batch);
		log.info("New batch created with id: {}", batch.getId());
		return batch;
	}

	public void save(long batchId, List<Price> prices) {
		log.info("Save batch with id: {}", id);
		final Batch batch = get(batchId);
		batch.upload(prices);
		log.info("Batch saved with id: {}", id);
	}

	public Collection<Price> commit(long batchId) {
		log.info("Committing batch with id: {}", id);
		final Batch batch = get(batchId);
		final Collection<Price> prices = batch.closeAndRefresh();
		currentBatches.remove(batchId);
		log.info("Batch committed with id: {}", id);
		return prices;
	}

	private Batch get(long batchId) {
		log.info("Get batch with id: {}", id);
		final Batch batch = currentBatches.get(batchId);
		if (batch == null) {
			throw new BatchNotFoundException(batchId);
		}
		return batch;
	}

}

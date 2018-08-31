package com.batch.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.batch.component.InMemoryCache;
import com.batch.exception.PriceNotFoundException;
import com.batch.model.Batch;
import com.batch.model.Price;
import com.batch.model.PriceFinder;
import com.batch.service.PriceService;

/**
 * 
 * @author faizanhussain
 *
 */
@Service
public class PriceServiceImpl implements PriceService {

	private static final Logger log = LoggerFactory.getLogger(PriceServiceImpl.class);
	private final ConcurrentHashMap<Long, Price> currentPrices = new ConcurrentHashMap<>();

	@Autowired
	private InMemoryCache cache;

	@Override
	public Batch createBatch() {
		return cache.create();
	}

	@Override
	public void upload(long batchId, List<Price> prices) {
		cache.save(batchId, prices);
	}

	@Override
	public void commit(long batchId) {
		log.info("Committing batch: {}", batchId);
		final Collection<Price> batchPrices = cache.commit(batchId);

		batchPrices.forEach(price -> currentPrices.compute(price.getId(), new PriceFinder(price)));
		log.info("Batch: {} is now committed", batchId);
	}

	@Override
	public void discard(long batchId) {
		log.info("Cancelling batch: {}", batchId);
		cache.commit(batchId);
		log.info("Batch: {} cancelled", batchId);
	}

	@Override
	public Price getPrice(long id) {
		final Price price = currentPrices.get(id);
		log.info("Latest price for id: {} is:  {}", id, price);
		if (price == null) {
			throw new PriceNotFoundException(id);
		}
		return price;
	}
}

package com.batch.service;

import java.util.List;

import com.batch.model.Batch;
import com.batch.model.Price;

/**
 * Interface for PriceService
 * 
 * @author faizanhussain
 *
 */
public interface PriceService {

	Batch createBatch();

	void upload(long batchId, List<Price> prices);

	void commit(long batchId);

	void discard(long batchId);

	Price getPrice(long id);

}

package com.batch.rest.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.batch.model.Batch;
import com.batch.model.Price;
import com.batch.service.PriceService;

/**
 * Rest API for batch.
 * 
 * @author faizanhussain
 *
 */
@RestController
@RequestMapping(path ="/batches")
class BatchController {

	private static final Logger log = LoggerFactory.getLogger(BatchController.class);

	@Autowired
	private PriceService priceService;

	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Long create() {
		Batch batch = priceService.createBatch();
		log.info("A new batch with id: {} has been created", batch.getId());
		return batch.getId();
	}

	@RequestMapping(value = "/{id}/upload", method = RequestMethod.PUT)
	@ResponseStatus(code=HttpStatus.NO_CONTENT)
	public void upload(@PathVariable long id, @RequestBody List<Price> prices) {
		log.info("Batch upload request received for id: {} with {} prices.", id, prices.size());
		priceService.upload(id, prices);
		log.info("Batch id: {} updated", id);
	}

	@RequestMapping(value = "/{id}/commit", method = RequestMethod.PUT)
	@ResponseStatus(code=HttpStatus.NO_CONTENT)
	public void commit(@PathVariable long id) {
		log.info("Batch commit request received for id: {}.", id);
		priceService.commit(id);
		log.info("Batch id: {} commited", id);
	}

	@RequestMapping(value = "/{id}/discard", method = RequestMethod.DELETE)
	@ResponseStatus(code=HttpStatus.NO_CONTENT)
	public void discard(@PathVariable long id) {
		log.info("Batch discard request received for id: {}.", id);
		priceService.discard(id);
		log.info("Batch id: {} discarded", id);
	}

}

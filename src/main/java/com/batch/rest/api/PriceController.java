package com.batch.rest.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.batch.model.Price;
import com.batch.service.PriceService;

/**
 * Rest API for prices.
 * 
 * @author faizanhussain
 *
 */
@RestController
@RequestMapping(path = "/prices")
public class PriceController {

	private static final Logger log = LoggerFactory.getLogger(PriceController.class);

	@Autowired
	private PriceService priceService;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	Price getLatest(@PathVariable(name = "id", required = true) long priceId) {
		log.debug("request received for price id: {}", priceId);
		return priceService.getPrice(priceId);
	}

}

package com.batch.model;

import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determine if the price is new using DateTime.
 * 
 * @author faizanhussain
 *
 */
public class PriceFinder implements BiFunction<Long, Price, Price> {

	private static final Logger log = LoggerFactory.getLogger(PriceFinder.class);

	private final Price newPrice;

	public PriceFinder(Price newPrice) {
		this.newPrice = newPrice;
	}

	@Override
	public Price apply(Long key, Price oldPrice) {
		Price price = oldPrice;
		if (isNewPrice(oldPrice))
			price = newPrice;
		log.info("Price id: {}, old price: {}, new price: {}, using: {}", key, oldPrice, newPrice, price);
		return price;
	}

	private boolean isNewPrice(Price oldPrice) {
		return oldPrice == null || oldPrice.getAsOf().isBefore(newPrice.getAsOf());
	}

}

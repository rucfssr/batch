package com.batch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Throw this exception when price is not found for a given identifier.
 * 
 * @author faizanhussain
 *
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PriceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 8788450065752050480L;

	public PriceNotFoundException(long priceId) {
		super(String.format("The price for identfier %s is unavailable.", priceId));
	}
}

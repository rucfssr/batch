package com.batch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for batch not found. Throw this exception when the batch is
 * invalid.
 * 
 * @author faizanhussain
 *
 */

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class BatchNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4784029314604203960L;

	public BatchNotFoundException(long id) {
		super(String.format("The batch for identfier %s is invalid.", id));
	}
}

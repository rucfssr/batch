package com.batch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for batch already processed/committed. Throw this exception when
 * the batch is already committed.
 * 
 * @author faizanhussain
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BatchAlreadyCommitted extends RuntimeException {
	private static final long serialVersionUID = 89212938508732965L;

	public BatchAlreadyCommitted(long id) {
		super(String.format("The batch for identfier %s has already been committed.", id));
	}
}

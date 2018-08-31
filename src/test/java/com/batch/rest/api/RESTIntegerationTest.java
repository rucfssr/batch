package com.batch.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.batch.Application;
import com.batch.model.Price;

/**
 * Integeration test for the application
 * 
 * @author faizanhussain
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class RESTIntegerationTest {

	private static final String DISCARD = "discard";

	private static final String PRICES = "prices";

	private static final String UPLOAD = "upload";

	private static final String CREATE = "create";

	private static final String COMMIT = "commit";

	private static final String URL_SEPERATOR = "/";

	private static final String BATCHES = "batches";

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private int port = 8080;

	@Autowired
	private TestRestTemplate template;

	private final String HOST = "http://localhost:" + port + URL_SEPERATOR;

	@Test
	public void getPriceShouldReturnNotFoundIfPriceIdIsNotAvailable() {
		ResponseEntity<Price> response = getPrice(1L);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void uploadBatchShouldReturnNotFoundIfBatchIdIsNotAvailable() {
		ResponseEntity<Void> response = upload(1L, Collections.singletonList(price(995945949L)));
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void createAndStartUploadOfBatchAndDiscardAndEnsurePriceFromUploadIsNotAccessible() {

		// create batch
		ResponseEntity<Long> createBatchResponse = create();
		assertEquals(HttpStatus.OK, createBatchResponse.getStatusCode());
		assertTrue(createBatchResponse.hasBody());
		assertNotNull(createBatchResponse.getBody());

		long batchId = createBatchResponse.getBody();

		// upload a price
		long priceId = 1000L;
		upload(batchId, Collections.singletonList(price(priceId)));

		// discard batch
		discard(batchId);

		// get price
		ResponseEntity<Price> priceResponse = getPrice(priceId);
		assertEquals(HttpStatus.NOT_FOUND, priceResponse.getStatusCode());
	}

	@Test
	public void createAndStartUploadOfBatchGetPriceBeforeCommitShouldNotBeAccessible() {

		// create batch
		ResponseEntity<Long> createBatchResponse = create();
		assertEquals(HttpStatus.OK, createBatchResponse.getStatusCode());
		assertTrue(createBatchResponse.hasBody());
		assertNotNull(createBatchResponse.getBody());

		long batchId = createBatchResponse.getBody();

		// upload
		Price price = price(2000L);
		upload(batchId, Collections.singletonList(price));

		// get price
		ResponseEntity<Price> priceResponse = getPrice(price.getId());
		assertEquals(HttpStatus.NOT_FOUND, priceResponse.getStatusCode());

		discard(batchId);

	}

	@Test
	public void createAndStartUploadOfBatchCommitAndGetPriceShouldBeAccessible() {

		// create batch
		ResponseEntity<Long> createBatchResponse = create();
		assertEquals(HttpStatus.OK, createBatchResponse.getStatusCode());
		assertTrue(createBatchResponse.hasBody());
		assertNotNull(createBatchResponse.getBody());

		long batchId = createBatchResponse.getBody();

		// upload
		Price price = price(3000L);
		upload(batchId, Collections.singletonList(price));

		// commit
		commit(batchId);

		// get price
		ResponseEntity<Price> priceResponse = getPrice(price.getId());
		assertEquals(HttpStatus.OK, priceResponse.getStatusCode());
	}

	@Test
	public void createAndStartUploadOfBatchCommitAndGetPriceShouldBeAccessibleAndUploadToSameBatchShouldReturnNotFound() {

		// create batch
		ResponseEntity<Long> createBatchResponse = create();
		assertEquals(HttpStatus.OK, createBatchResponse.getStatusCode());
		assertTrue(createBatchResponse.hasBody());
		assertNotNull(createBatchResponse.getBody());

		long batchId = createBatchResponse.getBody();

		// upload
		Price price = price(4000L);
		upload(batchId, Collections.singletonList(price));

		// commit
		commit(batchId);

		// get price
		ResponseEntity<Price> priceResponse = getPrice(price.getId());
		assertEquals(HttpStatus.OK, priceResponse.getStatusCode());

		// upload again
		Price price2 = price(5000L);
		ResponseEntity<Void> response = upload(batchId, Collections.singletonList(price2));
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

	}

	@Test
	public void commitABatchWhichIsAlreadyCommittedShouldReturnNotFound() {

		// create batch
		ResponseEntity<Long> createBatchResponse = create();
		assertEquals(HttpStatus.OK, createBatchResponse.getStatusCode());
		assertTrue(createBatchResponse.hasBody());
		assertNotNull(createBatchResponse.getBody());

		long batchId = createBatchResponse.getBody();

		// upload
		Price price = price(6000L);
		upload(batchId, Collections.singletonList(price));

		// commit
		commit(batchId);

		// get price
		ResponseEntity<Price> priceResponse = getPrice(price.getId());
		assertEquals(HttpStatus.OK, priceResponse.getStatusCode());

		// commit again
		ResponseEntity<Void> response = commit(batchId);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

	}

	@Test
	public void createBatchesAndUploadToEnsureLatestPricesAreVisible() {

		BatchRunner expected = new BatchRunner(6);
		List<Future<?>> tasks = new ArrayList<>();

		tasks.add(executorService.submit(new BatchRunner(1)));
		tasks.add(executorService.submit(new BatchRunner(2)));
		tasks.add(executorService.submit(new BatchRunner(3)));
		tasks.add(executorService.submit(new BatchRunner(4)));
		tasks.add(executorService.submit(new BatchRunner(5)));
		tasks.add(executorService.submit(expected));

		tasks.forEach(t -> {
			try {
				t.get(5, TimeUnit.SECONDS);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		LongStream.range(7000, 7500).forEach(priceId -> validateBatch(priceId, expected.t1));
		LongStream.range(7000, 7500).forEach(priceId -> validateBatch(priceId, expected.t2));
		LongStream.range(7000, 7500).forEach(priceId -> validateBatch(priceId, expected.t3));
		LongStream.range(7000, 7500).forEach(priceId -> validateBatch(priceId, expected.t4));
		LongStream.range(7000, 7500).forEach(priceId -> validateBatch(priceId, expected.t5));
	}

	private ResponseEntity<Price> getPrice(long priceId) {
		return template.getForEntity(HOST + PRICES + URL_SEPERATOR + priceId, Price.class);
	}

	private ResponseEntity<Void> upload(Long batchId, List<Price> prices) {
		return template.exchange(HOST + BATCHES + URL_SEPERATOR + batchId + URL_SEPERATOR + UPLOAD, HttpMethod.PUT,
				new HttpEntity<>(prices), Void.class);
	}

	private ResponseEntity<Void> commit(Long batchId) {
		return template.exchange(HOST + BATCHES + URL_SEPERATOR + batchId + URL_SEPERATOR + COMMIT, HttpMethod.PUT,
				null, Void.class);
	}

	private ResponseEntity<Long> create() {
		return template.exchange(HOST + BATCHES + URL_SEPERATOR + CREATE, HttpMethod.POST, null, Long.class);
	}

	private void discard(long batchId) {
		template.exchange(HOST + BATCHES + URL_SEPERATOR + batchId + URL_SEPERATOR + DISCARD, HttpMethod.DELETE, null,
				Void.class);
	}

	private Price price(long id) {
		return new Price(id, LocalDateTime.now(), UUID.randomUUID().toString());
	}

	private void validateBatch(long id, LocalDateTime expectedTime) {
		final ResponseEntity<Price> priceResponse = getPrice(id);
		assertEquals(HttpStatus.OK, priceResponse.getStatusCode());
		assertEquals(expectedTime, priceResponse.getBody().getAsOf());
	}

	private class BatchRunner implements Runnable {

		private LocalDateTime t1;
		private LocalDateTime t2;
		private LocalDateTime t3;
		private LocalDateTime t4;
		private LocalDateTime t5;

		BatchRunner(int hours) {
			t1 = LocalDateTime.now().plusHours(hours);
			t2 = LocalDateTime.now().plusHours(hours);
			t3 = LocalDateTime.now().plusHours(hours);
			t4 = LocalDateTime.now().plusHours(hours);
			t5 = LocalDateTime.now().plusHours(hours);
		}

		@Override
		public void run() {

			List<List<Price>> prices = Arrays.asList(
					LongStream.range(7000, 7500).mapToObj(new PriceMapper(t1)).collect(Collectors.toList()),
					LongStream.range(7000, 7500).mapToObj(new PriceMapper(t2)).collect(Collectors.toList()),
					LongStream.range(7000, 7500).mapToObj(new PriceMapper(t3)).collect(Collectors.toList()),
					LongStream.range(7000, 7500).mapToObj(new PriceMapper(t3)).collect(Collectors.toList()),
					LongStream.range(7000, 7500).mapToObj(new PriceMapper(t3)).collect(Collectors.toList()));

			Long batchId = create().getBody();
			prices.forEach(batch -> upload(batchId, batch));
			commit(batchId);
		}
	}
}

class PriceMapper implements LongFunction<Price> {
	private LocalDateTime asOf;

	PriceMapper(LocalDateTime asOf) {
		this.asOf = asOf;
	}

	@Override
	public Price apply(long id) {
		return new Price(id, asOf, UUID.randomUUID().toString());
	}
}

import java.sql.Timestamp;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

public class DataUploaderService {

	private final HttpClient client;

	public DataUploaderService(HttpClient client) {
		this.client = client;
	}

	public Mono<Void> upload(Flux<OrderedByteBuffer> input) {
		// TODO: send data to a server using the given client
		// TODO: MAX amount of sent buffers MUST be less or equals to 50 per request
		// TODO: frequency of client#send invocation MUST be not often than once per 500 Milliseconds
		// TODO: delivered results MUST be ordered
		// TODO: in case if send operation take more than 1 second it MUST be considered as hanged and be restarted

		// HINT: consider usage of .windowTimeout, onBackpressureBuffer, concatMap, timeout, retryWhen or retryBackoff
		return input
				.windowTimeout(50, Duration.ofMillis(500))
				.onBackpressureBuffer(Integer.MAX_VALUE, BufferOverflowStrategy.ERROR)
				.concatMap(buffers -> {
					long startSendTime = System.currentTimeMillis();

					return client.send(buffers)
							.timeout(Duration.ofSeconds(1))
							.retryWhen(Retry.max(5).filter((error) -> error instanceof TimeoutException))
							.then(Mono.defer(() -> {
								long currentDelay = System.currentTimeMillis() - startSendTime;
								return currentDelay < 500 ? Mono.delay(Duration.ofMillis(500 - currentDelay)).then() : Mono.empty();
							}));
				})
				.reduce((state, value) -> value);
	}
}

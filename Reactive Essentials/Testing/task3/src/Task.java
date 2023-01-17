import java.time.Duration;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

public class Task {

	public static void unitTestAFunction(Function<Flux<String>, Flux<Long>> functionToTest) {
		testSuccessCase(functionToTest);
		testFailureCase(functionToTest);
	}

	static void testSuccessCase(Function<Flux<String>, Flux<Long>> functionToTest) {
		// produce "1" "2" "100"
		StepVerifier.create(
				TestPublisher
						.<String>createCold()
						.emit("1", "2", "100")
						.flux().transform(functionToTest)
				)
				.expectSubscription()
				.expectNext(1L, 2L, 100L)
				.expectComplete()
				.verify();
	}

	static void testFailureCase(Function<Flux<String>, Flux<Long>> functionToTest) {
		StepVerifier.create(
						TestPublisher
								.<String>createCold()
								.emit("non number string")
								.flux().transform(functionToTest)
				)
				.expectSubscription()
				.expectError(NumberFormatException.class)
				.verify();
	}
}
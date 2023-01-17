import java.util.concurrent.Callable;

import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@SuppressWarnings({"ConstantConditions", "BlockingMethodInNonBlockingContext"})
public class Task {

	public static Flux<Long> checkAndDebug(Flux<Long> flux) {
		BlockHound.install(blockHoundIntegration);
		Hooks.onOperatorDebug();
		// 1) Add Better error printing
		return flux
				.filter(n -> n != 0)
				.scan(0L, (aLong, aLong2) -> (aLong + aLong2 + 2 * aLong) / aLong2)
				.filter(n -> n != 0)
				.flatMap(Task::doWork)
				.log()
				.retry(5);
	}

	private static Mono<Long> doWork(Long e) {
		return Mono.fromCallable(new MyCallable())
		           .zipWith(Mono.just(e))
				   .map(t2 -> t2.getT1() / t2.getT2());
	}

	private static BlockHoundIntegration blockHoundIntegration = new BlockHoundIntegration() {
		@Override
		public void applyTo(BlockHound.Builder builder) {
			builder
					.allowBlockingCallsInside(Task.class.getName(), "checkAndDebug")
					.allowBlockingCallsInside(Task.MyCallable.class.getName(), "call");
		}
	};

	public static class MyCallable implements Callable<Long> {

		@Override
		public Long call() throws Exception {
			Thread.sleep(100);
			return 1L;
		}
	}
}
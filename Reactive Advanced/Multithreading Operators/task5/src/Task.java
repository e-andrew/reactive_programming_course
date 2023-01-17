import java.util.concurrent.Callable;

import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class Task {

	public static Publisher<String> paralellizeLongRunningWorkOnUnboundedAmountOfThread(
			Flux<Callable<String>> streamOfLongRunningSources) {
		Scheduler scheduler = Schedulers.newBoundedElastic(256, 1, "schedulerName");
		return streamOfLongRunningSources.flatMap((callable) -> Mono.fromCallable(callable).subscribeOn(scheduler));
	}
}
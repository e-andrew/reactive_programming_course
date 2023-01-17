import org.reactivestreams.Publisher;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class Task {

	public static Publisher<String> replayLast3ElementsInHotFashion1(Flux<String> coldSource) {
		return coldSource.replay(3).refCount();
	}

	public static Publisher<String> replayLast3ElementsInHotFashion2(Flux<String> coldSource) {
		final Sinks.Many<String> replaySink = Sinks.many().replay().limit(3);

		coldSource.subscribe(new BaseSubscriber<String>() {
			@Override
			protected void hookOnNext(String value) {
				replaySink.tryEmitNext(value);
			}

			@Override
			protected void hookOnComplete() {
				replaySink.tryEmitComplete();
			}

			@Override
			protected void hookOnError(Throwable throwable) {
				replaySink.tryEmitError(throwable);
			}
		});

		return replaySink.asFlux();
	}
}
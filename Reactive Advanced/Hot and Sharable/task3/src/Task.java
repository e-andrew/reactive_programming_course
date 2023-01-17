import org.reactivestreams.Publisher;
import reactor.core.publisher.*;

public class Task {

	public static Publisher<String> transformToHotUsingProcessor(Flux<String> coldSource) {
		Sinks.Many<String> withoutBackpressureSink = Sinks.many().multicast().directBestEffort();

		coldSource.subscribe(new BaseSubscriber<String>() {
			@Override
			protected void hookOnNext(String value) {
				withoutBackpressureSink.tryEmitNext(value);
			}

			@Override
			protected void hookOnComplete() {
				withoutBackpressureSink.tryEmitComplete();
			}

			@Override
			protected void hookOnError(Throwable throwable) {
				withoutBackpressureSink.tryEmitError(throwable);
			}
		});

		return withoutBackpressureSink.asFlux();
	}
}
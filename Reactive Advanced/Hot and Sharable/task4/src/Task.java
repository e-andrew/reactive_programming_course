import org.reactivestreams.Publisher;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class Task {

	public static Publisher<String> transformToHot2(Flux<String> coldSource) {
		Sinks.Many<String> withBackpressureSink = Sinks.many().multicast().onBackpressureBuffer();

		coldSource.subscribe(new BaseSubscriber<String>() {
			@Override
			protected void hookOnNext(String value) {
				withBackpressureSink.tryEmitNext(value);
			}

			@Override
			protected void hookOnComplete() {
				withBackpressureSink.tryEmitComplete();
			}

			@Override
			protected void hookOnError(Throwable throwable) {
				withBackpressureSink.tryEmitError(throwable);
			}
		});

		return withBackpressureSink.asFlux();
	}
}
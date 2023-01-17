import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class Task {

	public static Publisher<String> combineSeveralSources(Publisher<String> prefixPublisher,
			Publisher<String> wordPublisher,
			Publisher<String> suffixPublisher) {

		return Flux.combineLatest(prefixPublisher, wordPublisher, suffixPublisher, (events) -> "" + events[0] + events[1] + events[2]);
	}
}
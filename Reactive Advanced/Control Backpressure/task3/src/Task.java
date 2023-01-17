import java.time.Duration;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;

public class Task {

	public static Flux<String> backpressureByBatching(Flux<Long> upstream,
			Duration duration) {
		return upstream.window(duration).flatMap(numbers -> numbers.map(Object::toString).reduce("", (acc, value) -> acc + value));
	}
}
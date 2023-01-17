import java.time.Duration;

import reactor.core.publisher.Mono;

public class ValidationTask {

	public static Mono<Void> validate(Duration duration) {
		if(!duration.isNegative() && !duration.isZero()){
			return Mono.empty();
		}
		return Mono.error(new IllegalArgumentException());
	}
}
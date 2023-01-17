import reactor.core.publisher.Flux;

public class Task {

	public static Flux<Character> createSequence(Flux<String> stringFlux) {
		return stringFlux.concatMap(e -> Flux.fromArray(e.split("")).map(s -> s.charAt(0)));
	}
}
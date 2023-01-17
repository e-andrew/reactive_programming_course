import reactor.core.publisher.Flux;

public class Task {

	public static Flux<Long> createSequence() {
		return Flux.generate(() -> STATE_ONE, (state, sink) -> {
			if(state.iteration == 20) sink.complete();
			sink.next(state.value);
			return new State(state.iteration + 1, state.previous.value + state.value, state);
		}).cast(Long.class).startWith(STATE_ZERO.value);
	}

	static class State {

		final State previous;
		final long  value;
		final long  iteration;

		State(long iteration, long value, State previous) {
			this.iteration = iteration;
			this.previous = previous;
			this.value = value;
		}
	}

	static final State STATE_ZERO  = new State(0, 0, null);
	static final State STATE_ONE = new State(1, 1, STATE_ZERO);
}
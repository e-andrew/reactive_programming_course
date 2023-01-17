import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class Task {

	public static Publisher<String> readFile(String filename) {
		return Flux.using(() -> Files.lines(Paths.get(filename)), Flux::fromStream, stringStream -> stringStream.close());
	}
}
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MediaService {

	private final ServersCatalogue catalogue;

	public MediaService(ServersCatalogue catalogue) {
		this.catalogue = catalogue;
	}

	public Mono<Video> findVideo(String videoName) {
		return Mono.firstWithValue(Flux.fromIterable(catalogue.list()).map(server -> server.searchOne(videoName)).toIterable());
	}
}

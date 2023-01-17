package service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.mongodb.MongoException;
import domain.Trade;
import domain.utils.DomainMapper;
import dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;
import repository.TradeRepository;
import repository.impl.H2TradeRepository;
import repository.impl.MongoTradeRepository;
import service.CryptoService;
import service.TradeService;
import service.utils.MessageMapper;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

public class DefaultTradeService implements TradeService {

	private static final Logger logger = LoggerFactory.getLogger("trade-service");

	private final Flux<MessageDTO<MessageDTO.Trade>> sharedStream;

	public DefaultTradeService(CryptoService service,
			TradeRepository jdbcRepository,
			TradeRepository mongoRepository
	) {
		service.eventsStream()
		       .transform(this::filterAndMapTradingEvents)
		       .transform(this::mapToDomainTrade)
		       .as(f -> this.resilientlyStoreByBatchesToAllRepositories(f, jdbcRepository, mongoRepository))
		       .subscribe();
		sharedStream = service.eventsStream()
		                      .transform(this::filterAndMapTradingEvents);
	}

	@Override
	public Flux<MessageDTO<MessageDTO.Trade>> tradesStream() {
		return sharedStream;
	}

	Flux<MessageDTO<MessageDTO.Trade>> filterAndMapTradingEvents(Flux<Map<String, Object>> input) {
		// TODO: Add implementation to produce trading events
		return input
				.filter(MessageMapper::isTradeMessageType)
				.map(MessageMapper::mapToTradeMessage);
	}

	Flux<Trade> mapToDomainTrade(Flux<MessageDTO<MessageDTO.Trade>> input) {
		// TODO: Add implementation to mapping to com.example.part_10.domain.Trade
		return input.map(DomainMapper::mapToDomain);
	}

	Mono<Void> resilientlyStoreByBatchesToAllRepositories(
			Flux<Trade> input,
			TradeRepository tradeRepository1,
			TradeRepository tradeRepository2) {

		Flux<Long> standardDuration = Flux.interval(Duration.ofSeconds(1));
		Sinks.Many<Long> nonStandardDuration = Sinks.many().unicast().onBackpressureBuffer();

		return input
				.buffer(Flux.zip(
						standardDuration,
						nonStandardDuration.asFlux().doOnSubscribe((s) -> nonStandardDuration.tryEmitNext(0L))))
				.concatMap(trades -> saveIntoRelationalDatabase(tradeRepository1, trades).zipWith(saveIntoMongoDatabase(tradeRepository2, trades))
						.doOnSuccess((t) -> nonStandardDuration.tryEmitNext(0L)))
				.then();
	}

	Mono<Integer> saveIntoMongoDatabase(TradeRepository tradeRepository1, List<Trade> trades) {
		return tradeRepository1
				.saveAll(trades)
				.timeout(Duration.ofSeconds(1))
				.retryWhen(Retry.backoff(100, Duration.ofMillis(100)))
				.then(Mono.just(trades.size()));
	}

	Mono<Integer> saveIntoRelationalDatabase(TradeRepository tradeRepository2, List<Trade> trades) {
		return tradeRepository2
				.saveAll(trades)
				.timeout(Duration.ofSeconds(1))
				.retryWhen(Retry.backoff(100, Duration.ofMillis(100)))
				.then(Mono.just(trades.size()));
	}

}

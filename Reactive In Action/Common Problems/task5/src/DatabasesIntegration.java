import java.time.Duration;
import java.util.concurrent.TimeoutException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class DatabasesIntegration {

	private final DatabaseApi oracleDb;
	private final DatabaseApi fileDb;

	public DatabasesIntegration(DatabaseApi oracleDb, DatabaseApi fileDb) {
		this.oracleDb = oracleDb;
		this.fileDb = fileDb;
	}

	public Mono<Void> storeToDatabases(Flux<Integer> integerFlux) {
		// TODO: Main) Write data to both databases
		// TODO: 1) Ensure Transaction is rolled back in case of failure
		// TODO: 2) Ensure All transactions are rolled back ion case any of write operations fails
		// TODO: 3) Ensure Transaction lasts less than 1 sec

		return integerFlux.publish(flux ->
				dbWriteInTransaction(oracleDb, flux)
						.zipWith(dbWriteInTransaction(fileDb, flux))
						.<Void>flatMap(transactions -> {
							Result r1 = transactions.getT1();
							Result r2 = transactions.getT2();

							if(r1 instanceof ErrorResult && r2 instanceof ErrorResult)
							{
								Throwable error = r1.error();
								error.addSuppressed(r2.error());
								return Mono.error(error);
							} else if(r1 instanceof ErrorResult) {
								return fileDb.rollbackTransaction(r2.transactionId()).then(Mono.error(r1.error()));
							} else if(r2 instanceof ErrorResult) {
								return oracleDb.rollbackTransaction(r1.transactionId()).then(Mono.error(r2.error()));
							} else {
								return Mono.empty();
							}
						})).then();
	}

    static Mono<Result> dbWriteInTransaction(DatabaseApi db, Flux<Integer> dataSource) {
        return Mono.usingWhen(db.<Integer>open().retryWhen(Retry.indefinitely().filter(error -> error instanceof IllegalAccessError)),
				connection -> connection.write(dataSource),
				Connection::close,
				(connection, error) -> connection.rollback().then(connection.close()),
				connection -> connection.rollback().then(connection.close()))
				.timeout(Duration.ofSeconds(3))
				.map(Result::ok)
				.onErrorResume(error -> Mono.just(Result.error(error)));
    }
}

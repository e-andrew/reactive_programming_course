import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserActivityUtils {

	public static Mono<Product> findMostExpansivePurchase(Flux<Order> ordersHistory,
			ProductsCatalog productsCatalog) {

		return ordersHistory.flatMap(order -> Flux.fromIterable(order.getProductsIds()).map(productsCatalog::findById)).reduce((state, value) -> state.getPrice() > value.getPrice() ? state : value);
	}
}
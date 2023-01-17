import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

public class Task {

	public static void dynamicDemand(Flux<String> source, CountDownLatch countDownOnComplete) {
		source.subscribe(new BaseSubscriber<String>() {
			long quantity = 1;
			long counter = 1;
			@Override
			protected void hookOnSubscribe(Subscription subscription){
				request(quantity);
			}

			@Override
			protected void hookOnNext(String value){
				counter -= 1;
				if(counter == 0){
					quantity *= 2;
					counter = quantity;
					request(quantity);
				}
			}

			@Override
			protected void hookOnComplete(){
				countDownOnComplete.countDown();
			}
		}
		);
	}
}
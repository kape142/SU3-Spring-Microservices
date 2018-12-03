package deliveryservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
en POST-metode, createOrder som tar imot varene i handlevogna, og returnerer en ordreId, med en gang.
en POST-metode, deliverOrder, som bare tar en ordreId som parameter og ikke returnerer noe.
	Denne metoden skal simulere skal sette status på ordren til "pending",
	 så etter x antall sekunder "under delivery", så etter x antall sekunder "delivered".
	Da kan den også sende mail til kunden om at den er klar for henting.
	deliverOrder må være synkron slik at klienten ikke trenger å vente på svar.
en GET-metode med ordreId i path'en som henter statusen på ordren.
 */

@RestController
public class DeliveryServiceController implements ApplicationListener<ApplicationReadyEvent> {

	private static ArrayList<Order> orders = new ArrayList<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryServiceController.class);

	@Autowired
	private WebClient.Builder webClientBuilder;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        LOGGER.info("DeliveryServiceController: init");
    }

	@PostMapping(value="/deliverorder/{id}")
	public Mono<Void> deliverOrder(@PathVariable("id") int id){
		LOGGER.info("deliverOrder");
    	Order order = orders.get(id);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		order.setStatus("Pending");
		executor.schedule(()->order.setStatus("Under delivery"), 15, TimeUnit.SECONDS);
		executor.schedule(()->order.setStatus("Delivered"), 30, TimeUnit.SECONDS);

		return Mono.empty();
	}

	@PostMapping(value="/createorder/{email}")
	public Mono<Integer> createOrder(@RequestBody int[] products, @PathVariable("email") String email){
		LOGGER.info("createOrder");
		int id = orders.size();
		orders.add(new Order(email, products));
		return Mono.just(id);
	}

	@GetMapping(value="/status/{id}")
	public Mono<String> getStatus(@PathVariable("id") int id) {
		LOGGER.info("getStatus: "+id);
		if(id>=orders.size()){
			return Mono.just("No such order: "+id);
		}
		return Mono.just(orders.get(id).getStatus());
	}

	@GetMapping(value="/test")
	public Mono<String> test() {
		LOGGER.info("test");
		return Mono.just("DeliveryServiceController is running");
	}

}
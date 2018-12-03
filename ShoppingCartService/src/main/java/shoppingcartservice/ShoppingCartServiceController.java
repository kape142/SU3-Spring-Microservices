package shoppingcartservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/*
en POST-metode for å legge til varer i kurven.
	Vi bruker epost for å identifisere brukeren og en produktId for å identifisere varen
en GET-metode som returnerer alle varene i handlevogna, med pris, gitt epost.
	Prisen skal hentes fra ProductService.
en POST-metode for å utføre kjøpet.
	Denne skal returnere en ordreId som den får fra DeliveryService.
	Den må også kalle deliverOrder på DeliveryService.
bruk gjerne statiske variabler for å lagre data
 */

@RestController
public class ShoppingCartServiceController implements ApplicationListener<ApplicationReadyEvent> {

	private static final HashMap<String, ArrayList<Integer>> shoppingCartMap = new HashMap<>();
	private static final Consumer conull= a->{};
	private static final Runnable runull= ()->{};

	private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingCartServiceController.class);

	@Autowired
	private WebClient.Builder webClientBuilder;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        LOGGER.info("ShoppingServiceController: init");
    }

	@GetMapping(value="/test")
	public Mono<String> test() {
		LOGGER.info("test");
		return Mono.just("ShoppingCartServiceController is running");
	}

	@PostMapping(value="/buy/{email}")
	public Mono<Integer> buyShoppingCart(@PathVariable("email") String email) {
		LOGGER.info("buyShoppingCart");
		WebClient webClient = webClientBuilder.baseUrl("http://delivery-service").build();
		Mono<Integer> idMono = webClient.post()
				.uri("/createorder/"+email)
				.body(BodyInserters.fromObject(shoppingCartMap.get(email).stream().mapToInt(i->i).toArray()))
				.retrieve()
				.bodyToMono(Integer.class);
		Mono<Integer> returnMono= Mono.create(emitter->
			idMono.subscribe(value->{
						webClient.post()
								.uri("/deliverorder/"+value)
								.retrieve().bodyToMono(Integer.class)
								.subscribe(
										val->LOGGER.info("should be empty: "+val),
										a->{},
										()->{}
										);
						LOGGER.info("value: "+value);
						emitter.success(value);
					},
					error->LOGGER.warn("error: "+error),
					()-> LOGGER.info("deliverorder sent")
			)
		);
		return returnMono;
	}

	@PostMapping(value="/addproduct/{email}/{productid}")
	public Mono<Void> addProductToShoppingCart(@PathVariable("email") String email,
											   @PathVariable("productid") int id) {
		LOGGER.info("addProductToShoppingCart");
		if(!shoppingCartMap.containsKey(email)){
			shoppingCartMap.put(email, new ArrayList<>());
		}
		shoppingCartMap.get(email).add(id);
		LOGGER.info(String.format("Product id %d added to shopping cart of user %s", id, email));
		return Mono.empty();
	}

	@GetMapping(value="/{email}")
	@ResponseBody
	public Flux<Product> getShoppingCartContents(@PathVariable("email") String email) {
    	LOGGER.info(String.format("getShoppingCartContents for %s", email));
    	ArrayList<Integer> idList = shoppingCartMap.get(email);
		Flux<Product> productFlux = Flux.create(emitter ->{
			final int[] finished = {0};
			for (int id : idList) {
				LOGGER.info(String.format("product id %d", id));
				Mono<Product> productMono = webClientBuilder.build().get()
						.uri("http://product-service/" + id).retrieve()
						.bodyToMono(Product.class);
				productMono.subscribe(
						value -> {
							LOGGER.info(value.getNavn());
							emitter.next(value);
						},
						emitter::error,
						()-> {
							LOGGER.info(String.format("product %d finished", id));
							if(++finished[0] == idList.size()){
								emitter.complete();
							}
						}
				);
			}
		});
		LOGGER.info("sending flux");
		productFlux.publish();
		return productFlux;
	}
}
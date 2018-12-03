package productservice;

import com.sun.org.apache.regexp.internal.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

/*
en GET-metode som returnerer navn og produktId for alle varene i sortimentet
en GET-metode med ID i path'en som returnerer prisen på en vare
Bruk en hardkodet liste eller map med produkter
 */

@RestController
public class ProductServiceController implements ApplicationListener<ApplicationReadyEvent> {

	private static final Product[] produkter = {
		new Product("Banan",0, 8),
		new Product("Mais",1, 6),
		new Product("Ketchup", 2, 25),
		new Product("Sjokolade", 3, 40),
		new Product("Coca Cola", 4, 30),
		new Product("Salt", 5, 15),
		new Product("Pepperkaker", 6, 45),
		new Product("Chips", 7, 20),
		new Product("Hamburger", 8, 50),
		new Product("Kyllingvinger", 9, 60),
	};

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceController.class);

	@Autowired
	private WebClient.Builder webClientBuilder;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        LOGGER.info("ProductServiceController: init");
    }

	@GetMapping(value="/test")
	public Mono<String> test() {
		LOGGER.info("test");
		return Mono.just("ProductServiceController is running");
	}

	@GetMapping(value="/all")
	public Flux<Product> getAll() {
		LOGGER.info("getAll");
		LOGGER.info("returning all products");
		return Flux.just(produkter);
	}

	@GetMapping(value="/{id}")
	@ResponseBody
	public ResponseEntity getOne(@PathVariable("id") int id) {
    	if(id>=produkter.length){
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such product");
		}
		return ResponseEntity.ok(Mono.just(produkter[id]));
	}
}
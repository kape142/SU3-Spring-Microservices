package service1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class Service1Controller implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Service1Controller.class);

	@Autowired
	private WebClient.Builder webClientBuilder;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        LOGGER.info("Service1Controller: init");

        // Subscribe to stream
        Flux<String> messages = webClientBuilder.build().get().uri("http://service-2/messages").retrieve().bodyToFlux(String.class);
        messages.subscribe(message -> LOGGER.info("subscriber: " + message));
    }

	@GetMapping()
	public Flux<Service1Bean> getAll() {
		LOGGER.info("getAll");
		waitFor2Seconds();

		LOGGER.info("returning flux");
		return Flux.just(new Service1Bean("1"), new Service1Bean("2"));
	}

	@PostMapping()
	Mono<Void> create(@RequestBody Service1Bean bean) {
		LOGGER.info("create: id={}", bean.getId());
		waitFor2Seconds();
		return Mono.empty();
	}

	private void waitFor2Seconds() {
		LOGGER.info("sleeping for 2 seconds");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
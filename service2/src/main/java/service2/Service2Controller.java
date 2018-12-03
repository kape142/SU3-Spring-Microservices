package service2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@RestController
public class Service2Controller implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Service2Controller.class);

	@Autowired
	private WebClient.Builder webClientBuilder;
    public MessagePublisher messagePublisher = new MessagePublisher();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        LOGGER.info("Starting publisher");
        Runnable helloRunnable = () -> {
            messagePublisher.sendMessage("Yo " + LocalDateTime.now().getSecond());
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 5, TimeUnit.SECONDS);
    }

    @GetMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getMessages() {
        Flux<String> f = Flux.create(fluxSink -> {
            messagePublisher.addObserver((o, arg) -> {
                LOGGER.info("Publishing message " + arg);
                fluxSink.next((String)arg);
            });
        });
        f.publish();
        return f;
    }

    @GetMapping()
	public Flux<Service2Bean> getAll() {
		LOGGER.info("getAll1");

		messagePublisher.sendMessage("call to getAll");

		// Post async to other microservice
		WebClient.RequestHeadersSpec p = webClientBuilder.build().post().uri("http://service-1").body(BodyInserters.fromObject(new Service1Bean("postId")));
		p.retrieve().bodyToMono(Void.class).subscribe();

		// Get data from other (slow) microservice
		LOGGER.info("getAll2");
		Flux<Service1Bean> beans = webClientBuilder.build().get().uri("http://service-1").retrieve().bodyToFlux(Service1Bean.class);

		// Map result to local datatype (in the future)
		Flux<Service2Bean> beans2 = beans.map(a -> new Service2Bean("Mapped:" + a.getId()));

		// Return (not yet ready) result
		LOGGER.info("getAll3 (should happen right after getAll1 and 2)");
		return beans2;
	}
}
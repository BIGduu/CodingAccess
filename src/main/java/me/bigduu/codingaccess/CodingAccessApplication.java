package me.bigduu.codingaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class CodingAccessApplication implements CommandLineRunner {
    private static final String SERVER_PORT = "server.port";
    public static final String UP = "UP";
    public static final String MASTER_HOST = "http://localhost:8080/ports/";
    private final WebClient webClient;

    public CodingAccessApplication(WebClient webClient) {
        this.webClient = webClient;
    }

//    @EventListener
    public void beforeShutdown(ContextStoppedEvent event) {
        Optional.ofNullable(System.getProperty(SERVER_PORT))
                .ifPresent(it -> webClient.delete().uri(MASTER_HOST + it)
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribe());
    }

    @Override
    public void run(String... args) {
        Optional.ofNullable(System.getProperty(SERVER_PORT))
                .ifPresent(it -> webClient.post().uri(MASTER_HOST + it)
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribe());
    }

    record Health(String status) {
    }

    public static void main(String[] args) {
        System.setProperty("sun.net.client.defaultConnectTimeout", "2000");
        final var restTemplate = new RestTemplate();
        try {
            Optional.of(restTemplate.getForEntity("http://localhost:8080/actuator/health", Health.class))
                    .map(HttpEntity::getBody)
                    .map(it -> it.status)
                    .filter(UP::equalsIgnoreCase)
                    .flatMap(it -> Optional.of(restTemplate.getForEntity("http://localhost:8080/ports/one", String.class)))
                    .filter(it -> it.getStatusCode().equals(HttpStatus.OK))
                    .filter(HttpEntity::hasBody)
                    .map(HttpEntity::getBody)
                    .ifPresent(it -> System.setProperty(SERVER_PORT, it));
        } catch (RestClientException e) {
            log.error("Request error", e);
        }
        SpringApplication.run(CodingAccessApplication.class, args);
    }
}

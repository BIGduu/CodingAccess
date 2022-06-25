package me.bigduu.codingaccess.slave.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/write")
public class WriteController {

    private static final String PATH = "tmp";

    static {
        Path tmp = Path.of("tmp");
        if (!Files.exists(tmp)) {
            try {
                Files.createDirectories(tmp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @PostMapping
    public Mono<String> write() {
        final var property = System.getProperty("server.port");
        final var l = System.currentTimeMillis();

        final var of = Path.of(PATH, l + "." + property);
        log.info("begin write hello file name {}", l);
        return Mono.fromSupplier(() -> {

            try {
                Files.write(of, "hello".getBytes());
                return "OK";
            } catch (IOException e) {
                log.error("Error write to {}", of.toAbsolutePath(), e);
                return "ERROR";
            }
        });
    }
}

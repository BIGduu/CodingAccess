package me.bigduu.codingaccess.master.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bigduu.codingaccess.master.service.RegisterService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ports")
public class RegisterController {
    private final RegisterService registerService;

    @GetMapping
    public Flux<Integer> getAllPorts() {
        return registerService.getPortInUse();
    }

    @GetMapping("/one")
    public Mono<Integer> getAnAvailablePorts() {
        return registerService.getAnAvailablePorts();
    }


    @PostMapping("/{port}")
    public Mono<Boolean> register(@PathVariable Integer port) {
        return registerService.register(port);
    }

    @DeleteMapping("/{port}")
    public Mono<Boolean> unregister(@PathVariable Integer port) {
        return registerService.unregister(port);
    }
}

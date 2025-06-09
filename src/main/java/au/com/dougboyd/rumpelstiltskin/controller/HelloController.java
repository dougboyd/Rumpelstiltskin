package au.com.dougboyd.rumpelstiltskin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple REST controller for greeting.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * GET  /api/hello
     */
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Rumpelstiltskin!";
    }

package au.com.dougboyd.rumpelstiltskin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple REST controller for greeting and code generation.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * GET /api/hello
     */
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Rumpelstiltskin!";
    }

    /**
     * POST /api/generate-code-directly
     * @param inputRequest the input string
     * @return confirmation message
     */
    @PostMapping("/generate-code-directly")
    public String generateCodeDirectly(@RequestParam String inputRequest) {
        return "this worked " + inputRequest;
    }
}

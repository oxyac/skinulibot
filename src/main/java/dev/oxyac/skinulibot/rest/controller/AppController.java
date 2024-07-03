package dev.oxyac.skinulibot.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController(
        value = "AppController"

)
public class AppController {
    @GetMapping(value = "/")
    public Integer getTestData() {
        return 200;
    }
}

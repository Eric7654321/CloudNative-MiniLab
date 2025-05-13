package minilab.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public String testString(){
        return "test";
    }
    @GetMapping("/time")
    public String getTime(){
        return LocalDateTime.now().toString();
    }
}

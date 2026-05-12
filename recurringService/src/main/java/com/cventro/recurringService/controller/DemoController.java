package com.cventro.recurringService.controller;

import com.cventro.recurringService.service.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    private MessageProducer messageProducer;

    @GetMapping("/demo")
    public String demo(){
        return "Recurring service is running";
    }
}

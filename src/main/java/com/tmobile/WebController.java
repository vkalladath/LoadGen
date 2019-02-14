package com.tmobile;

import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
public class WebController {
    private static long count = 0;

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
    
    @Value("${MIN_LATENCY:0}")
    private int minLatency = 0;
    
    @Value("${MAX_LATENCY:0}")
    private int maxLatency = 0;
    
    @Value("${RESPONSE_SIZE:1024}")
    private int responseSize;

    @RequestMapping("/")
    public String index() {
        String randomText = randomAlphaNumeric(responseSize);
        latency(minLatency, maxLatency);
        return "<h1>" + count++ + "</h1>\r\n<p>" + randomText;
    }

    private void latency(int minLatency, int maxLatency) {
        if (minLatency == maxLatency){
            sleep(minLatency);
        } else if (maxLatency > minLatency) {
            Random random = new Random();
            sleep(minLatency + random.nextInt(maxLatency - minLatency) + 1);
        }
    }

    private void sleep(int latency) {
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
        }
    }

}
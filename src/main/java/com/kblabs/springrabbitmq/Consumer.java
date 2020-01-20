package com.kblabs.springrabbitmq;

import ch.qos.logback.core.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class Consumer {

    protected transient Logger log = Logger.getLogger(this.getClass().getName());

    public void receiveMessage(String message) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Received : " + message);
    }

}

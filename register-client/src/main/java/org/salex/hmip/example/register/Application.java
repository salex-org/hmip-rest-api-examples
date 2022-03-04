package org.salex.hmip.example.register;

import org.salex.hmip.client.HmIPConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;

/**
 * Spring Boot command line app to register a new client.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Autowired
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        if(args.length < 2) {
            LOG.error("Missing arguments");
            LOG.info("Usage: <executable> <access-point-sgtin> <client-name> <pin>");
            System.exit(1);
        } else {
            SpringApplication.run(Application.class, args);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        final var accessPointSGTIN = args[0];
        final var clientName = args[1];
        var pin = "";
        if(args.length > 2) {
            pin = args[2];
        }
        HmIPConfiguration.builder()
                .clientName(clientName)
                .accessPointSGTIN(accessPointSGTIN)
                .pin(pin)
                .build()
                .flatMap(config -> config.registerClient())
                .doOnError(error -> {
                    LOG.error(String.format("Failed to register new client: %s", error.getMessage()));
                    SpringApplication.exit(context);
                    System.exit(1);
                }).subscribe(config -> {
                    LOG.info("Successfully registered new client");
                    LOG.info(String.format("Device ID: %s", config.getDeviceId()));
                    LOG.info(String.format("Client ID: %s", config.getClientId()));
                    LOG.info(String.format("Client Auth Token: %s", config.getClientAuthToken()));
                    LOG.info(String.format("Auth Token: %s", config.getAuthToken()));
                    System.exit(SpringApplication.exit(context));
                });
    }
}

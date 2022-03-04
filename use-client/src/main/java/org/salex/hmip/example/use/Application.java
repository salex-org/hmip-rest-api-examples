package org.salex.hmip.example.use;

import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;

import java.util.Map;

/**
 * Spring Boot command line app to use an already registered client.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = HmIPProperties.class)
public class Application implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private HmIPClient client;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        client.loadCurrentState()
                .doOnError(error -> {
                    LOG.error(String.format("Failed to load the current state: %s", error.getMessage()), error);
                    SpringApplication.exit(context);
                    System.exit(1);
                })
                .subscribe(currentState -> {
                    LOG.info("Successfully loaded the current state");
                    LOG.info("Clients:");
                    for (var client:currentState.getClients().values()) {
                        LOG.info(String.format("\t%s (%s)", client.getName(), client.getId()));
                    }
                    LOG.info("Device:");
                    for (var device:currentState.getDevices().values()) {
                        LOG.info(String.format("\t%s (%s), Type: %s, Model: %s", device.getName(), device.getId(), device.getType(), device.getModel()));
                        if("TEMPERATURE_HUMIDITY_SENSOR_OUTDOOR".equals(device.getType())) {
                            var temperature = Double.valueOf(((Map<String, Object>)device.getChannels().get("1")).get("actualTemperature").toString());
                            var humidity = Integer.valueOf(((Map<String, Object>)device.getChannels().get("1")).get("humidity").toString());
                            LOG.info(String.format("\t\tMeasurement from %s: %.1f°C and %d%% humidity", device.getStatusTimestamp(), temperature, humidity));
                        }
                    }
                    System.exit(SpringApplication.exit(context));
                });
    }
}

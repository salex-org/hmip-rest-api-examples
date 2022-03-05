package org.salex.hmip.example.use;

import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPProperties;
import org.salex.hmip.client.HmIPState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

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
        client.getDevice("3014F711A00010DD899E53A0")
                .doOnError(error -> {
                    LOG.error(String.format("Failed to load the current state: %s", error.getMessage()), error);
                    SpringApplication.exit(context);
                    System.exit(1);
                })
                .map(device -> {
                    LOG.info("Successfully loaded current state of the climate sensor");
                    LOG.info(String.format("Measurement from %s", device.getStatusTimestamp()));
                    return device.getChannels().values();
                })
                .flatMapMany(Flux::fromIterable)
                .filter(channel -> channel instanceof HmIPState.ClimateSensorChannel)
                .next()
                .cast(HmIPState.ClimateSensorChannel.class)
                .subscribe(channel -> {
                    LOG.info(String.format("%.1f°C and %d%% humidity", channel.getTemperature(), channel.getHumidity()));
                    System.exit(SpringApplication.exit(context));
                });
    }
}

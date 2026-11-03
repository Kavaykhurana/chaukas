package in.chaukas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ChaukasApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChaukasApplication.class, args);
    }
}

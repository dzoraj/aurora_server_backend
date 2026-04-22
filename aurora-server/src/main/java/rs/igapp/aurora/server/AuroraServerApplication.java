package rs.igapp.aurora.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "rs.igapp.aurora")
@EntityScan(basePackages = "rs.igapp.aurora.domain.entity")
@EnableJpaRepositories(basePackages = "rs.igapp.aurora.persistence.repository")
public class AuroraServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuroraServerApplication.class, args);
    }
}
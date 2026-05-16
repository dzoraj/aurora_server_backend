package rs.igapp.aurora.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import rs.igapp.aurora.agent.config.AgentProperties;

@SpringBootApplication(scanBasePackages = "rs.igapp.aurora.agent")
@EnableConfigurationProperties(AgentProperties.class)
public class AuroraAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuroraAgentApplication.class, args);
    }
}

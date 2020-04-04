package pl.ks.flame.graph;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalFlameGraphConfiguration {
    @Bean
    ExternalFlameGraphExecutor externalFlameGraphExecutor(@Value("${external.flame-graph.path}") String flameGraphPath) {
        return new ExternalFlameGraphExecutor(flameGraphPath);
    }
}

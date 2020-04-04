package pl.ks.collapsed.stack.viewer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pl.ks.flame.graph.ExternalFlameGraphConfiguration;

@Import({
        ExternalFlameGraphConfiguration.class
})
@Configuration
class CollapsedStackViewerApplicationConfiguration {
    @Bean
    CollapsedStackPageCreator collapsedStackPageCreator() {
        return new CollapsedStackPageCreator();
    }
}

package net.camelapp.routes

import net.camelapp.services.ClaimProcessorB
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

/**
 * Demonstrating that routes can be created in separate classes.
 */
@Component
class ClaimProcessorBRoute extends RouteBuilder {

    @Override
    void configure() throws Exception {
        System.out.println("${this.getClass()}: " + restConfiguration())
        from('rabbitmq:claimTypeB')
                .routeId('claimTypeB')
                .log('>>> To processorB: ${body}')
                .delayer(2000) // to simulate work being done
                .bean(ClaimProcessorB, 'process')
                .end()
    }
}

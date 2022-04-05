package net.camelapp.routes

import groovy.util.logging.Slf4j
import org.apache.camel.ExchangeProperties
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import net.camelapp.DtoConverter
import net.camelapp.models.Payload

import net.camelapp.services.ClaimProcessorD
import net.camelapp.services.ClaimProcessorA

import static net.camelapp.AppConfig.SEDA_ASYNC_OPTION

@Component
class ClaimProcessorRoute extends RouteBuilder {

    @Override
    void configure() throws Exception {
        from('seda:claim-router')
                .routeId('routing-claim')
                // Use Properties not Headers
                // https://examples.javacodegeeks.com/apache-camel-headers-vs-properties-example/
                .setProperty('contention_type', simple('${body.contention_type}'))
//                .tracing()
                .dynamicRouter(method(DynamicClaimRouter, 'route'))

        from('seda:claimTypeA')
                .routeId('seda-claimTypeA')
//                .tracing()
                .log('>>> To processorA: ${body}')
                .delayer(2000) // to simulate work being done
                .bean(ClaimProcessorA, 'process')
                .end()

        from('rabbitmq:claimTypeD')
                .routeId('claimTypeD')
//                .tracing()
                .log('>>> To processorD: ${body}')
                .delayer(2000) // to simulate work being done
                .bean(ClaimProcessorD, 'process')
                .end()
    }

    @Slf4j
    static class DynamicClaimRouter {
        /**
         * Use this method to compute dynamic where we should route next.
         *
         * @param body the message body
         * @param props the exchange properties where we can store state between invocations
         * @return endpoints to go, or <tt>null</tt> to indicate the end
         */
        String route(Object body, @ExchangeProperties Map<String, Object> props) throws IOException {
            // get the state from the exchange props and keep track how many times it has been invoked
            int invoked = 0
            Object current = props.get('invoked')
            if (current != null) {
                invoked = Integer.parseInt(current.toString())
            }
            invoked++
            // and store the state back on the props
            props.put('invoked', invoked)

            String claimType = (String) props.get('contention_type')
            if (invoked == 1) {
                switch (claimType) {
                    case 'A':
                        return 'seda:claimType' + claimType // wait for result // + '?' + SEDA_ASYNC_OPTION
                    case 'B': // Groovy
                    case 'C': // Ruby in separate process
                    case 'D': // JRuby
                        System.err.println('sending to rabbitmq:claimType' + claimType)
                        return "rabbitmq:claimType${claimType}"
                    default:
                        System.err.println('ERROR: unknown contention_type: ' + claimType)
                        return null
                }
            } else if (invoked == 2) {
                String submission_id
                if (body instanceof Payload)
                    submission_id = ((Payload) body).getSubmission_id()
                else if (body instanceof byte[])
                    submission_id = DtoConverter.toPojo(Payload, (byte[]) body).getSubmission_id()
                else
                    throw new IllegalArgumentException('body ' + body.getClass())

                log.info(">>> Claim processed ${claimType}: ${submission_id} ")
                return "seda:claim-rrd-processed-${submission_id}?${SEDA_ASYNC_OPTION}"
            }

            // no more so return null
            null
        }
    }
}

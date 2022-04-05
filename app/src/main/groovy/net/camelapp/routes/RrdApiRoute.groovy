package net.camelapp.routes

import org.apache.camel.AggregationStrategy
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import net.camelapp.services.ClaimService
import net.camelapp.models.Claim
import net.camelapp.models.Payload

import static net.camelapp.AppConfig.SEDA_ASYNC_OPTION

@Component
class RrdApiRoute extends RouteBuilder {

    @Override
    void configure() throws Exception {
        // println getClass() + ": " + restConfiguration()

        rest('/claims')
                // Using javax.ws.rs.core.MediaType.APPLICATION_JSON cause error in Groovy
                // see https://stackoverflow.com/questions/14447650/not-able-to-use-variables-defined-in-classes-within-groovy-annotations
                // Declares API expectations in results of /api-doc endpoint
                .consumes('application/json')
                .produces('application/json')

                // POST
                .post('/').description('Add claim')
                .type(Claim)
                .outType(Claim)
                .route()
                .routeId('rest-POST-claim')
                .tracing()
                .log('>>1> ${body.getClass()}')
                .to('seda:addClaim') // save Claim to DB and assign UUID before sending it to recipientList
                .recipientList(constant('seda:logToFile?' + SEDA_ASYNC_OPTION +
                        ',seda:claim-router?' + SEDA_ASYNC_OPTION))
                .parallelProcessing()
                .log('>>4> ${body.toString()}')
                .endRest()

                // GET
                .get('/').description('Get all claims')
                .outType(Claim[])
                .route()
                .routeId('claims-getAll')
                .bean(ClaimService, 'getAllClaims')
                .endRest()

                // GET
                .get('/{id}').description('Get claim')
                .outType(Claim)
                .route()
                .routeId('claims-getById')
                // https://camel.apache.org/components/3.14.x/languages/simple-language.html#_variables
                .setBody(simple('${header.id}'))
                .bean(ClaimService, 'getClaim')
                .log('>>3> ${body.toString()}')
                .endRest()

                // GET details
                .get('/details/{id}').description('Get claim')
                .outType(Claim)
                .route()
                .routeId('claimDetails-getById')
                .bean(ClaimService, 'claimDetail')
                .endRest()

                // GET
                .get('/{id}/status-diff-from/{status}')
                .description('Returns the claim when it changes from specified status')
                .outType(Claim)
                .route()
                .routeId('claim-status-change')
                .setBody(simple('${header.id}'))
                // subscribe on queue, waiting for specified claim to complete
                .pollEnrich(simple('seda:claim-rrd-processed-${header.id}?multipleConsumers=true'), -1, new ChooseSecondExchangeStrategy(), false)
// This works too:
//                    .process(exchange -> {
//                        String headerId=exchange.getMessage().getHeader("id", String)
//                        Endpoint endpoint = exchange.getContext().getEndpoint("seda:claim-rrd-processed?multipleConsumers=true")
//                        PollingConsumer consumer = endpoint.createPollingConsumer()
//                        Payload body=null
//                        do {
//                            Exchange existingExchange = consumer.receive()
//                            body = existingExchange.getMessage().getBody(Payload)
//                            System.out.println("submission_id is "+body.getSubmission_id())
//                        } while(!headerId.equals(body.getSubmission_id()))
//                        exchange.getMessage().setBody(body)
//                    })
                .log('>>5> diff status?: ${body}')
                .convertBodyTo(Payload)
                .endRest()

        from('seda:addClaim')
                .log('>>2> ${body.getClass()}')
                .bean(ClaimService, 'addClaim')

        from('seda:logToFile')
                .marshal().json()
                .log('>>3> ${body.getClass()}')
                .to('file://build/newClaims')

        from('seda:claim-rrd-processed?multipleConsumers=true')
                .log('>>>>>>>>> RRD processed! claim: ${body.toString()}')
    }

    static class ChooseSecondExchangeStrategy implements AggregationStrategy {

        Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            println '------------- ChooseSecondExchangeStrategy'
            if (newExchange == null) {
                return oldExchange
            } else {
                return newExchange
            }
//            Object oldBody = oldExchange.getIn().getBody()
//            Object newBody = newExchange.getIn().getBody()
//            oldExchange.getIn().setBody(oldBody + ":" + newBody)
//            return oldExchange
        }

    }
}
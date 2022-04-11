
## Goals

This project is skeleton code demonstrating how a Spring-based app uses Apache Camel to implement a microservice architecture.

- Allow folks to familiarize themselves with Java, Gradle, Spring, and Camel tools
- Enables folks to experiment with the code

## Files and directories

- `init.log` - describes how this project was created, along with links to reading materials
- `sourceme.sh` - convenience shell functions
- `app` directory - created by Gradle for the application code. For simplicity, classes for the model, view, controller, services, and Camel routes are saved in this project rather than subprojects.
- `app/build.gradle` - primary build file
- `list` and `utilities` directories - created by Gradle to demonstrate subprojects
- `buildSrc` - created by Gradle to demonstrate sharable build configurations

All the new code is within the `app` directory. The other directories are left for learning and experimenting with Gradle.

Also take a look at the first few commits in this repo. They follow the steps documented in `init.log`.

### Software Design
- QP (Queue-Processor(s)) component acts like an internal microservice that modularizes functionalities so that it can be updated and maintained more easily
    - Consists of one *Queue*, which has a globally unique *name* and simply holds items. Items can be added to the queue by anything. Queue’s contents should be persisted to restore RRD state in case of system failure.
    - A *Processor* processes items from the Queue. They are stateless and preferably idempotent. A Processor can be implemented in practically any language (Java, Ruby, Python, etc.), as long as it can interface with a [Message Queue](https://en.wikipedia.org/wiki/Message_queue) (in this case, [RabbitMQ](https://en.wikipedia.org/wiki/RabbitMQ) but [Amazon SQS](https://en.wikipedia.org/wiki/Amazon_Simple_Queue_Service) could be used). For scalability, a Processor can be replicated to process items from the Queue in parallel — shown as “instance 1” and “instance 2” in the diagram.

```mermaid
flowchart LR
something1[some thing] & anotherthing[/another thing/] -.item.-> someQ

subgraph some-qp[x QP]
  someQ([x Queue]) --> someProcess[/x Processor\ninstance 1/] & someProcess2[/x Processor\ninstance 2/]
end

classDef queue fill:#0ee,stroke:#333,stroke-width:4px
class someQ queue
classDef processor fill:#f9f,stroke:#333,stroke-width:2px
class anotherthing,someProcess,someProcess2 processor
```

- QP components will be connected together using well-tested and stable [Enterprise Integration Patterns](https://en.wikipedia.org/wiki/Enterprise_Integration_Patterns) (EIP) tools (such as [Apache Camel](https://camel.apache.org/manual/faq/what-is-camel.html)) so that we can focus on RRD functionality and less on “glue code”.
    - As the RDD prototype becomes more complex, using the same QP pattern for all RRD functionalities promotes low [software coupling](https://en.wikipedia.org/wiki/Coupling_(computer_programming)) and, as a result, simplifies debugging and maintenance.
    - Using EIPs facilitate future migration of parts of the architecture to cloud-based services if needed (see [Implementing EIP with AWS](https://aws.amazon.com/blogs/compute/implementing-enterprise-integration-patterns-with-aws-messaging-services-point-to-point-channels/) and [with Azure](https://docs.microsoft.com/en-us/azure/architecture/reference-architectures/enterprise-integration/queues-events)), where for example a Processor could be implemented as AWS Lambda.
    - Using the Apache Camel implementation of EIPs provides [time-saving features](https://stackoverflow.com/a/11540451) and integrations with many existing [tools](https://camel.apache.org/components/3.15.x/index.html) and [data formats](https://camel.apache.org/components/3.15.x/dataformats/index.html).
    - Example workflow using QP components:
```mermaid
flowchart TB

rrd-api[[RRD API]] -.claim.-> newClaimsQ([new claims Q])

subgraph router-qp[Router QP]
  newClaimsQ --> router[/Claim Router/]
end

router -.skip RDD.-> rrdDoneQ
router -.-> htnQ & asthmaQ & apneaQ & manualQ

subgraph htn-qp["htn QP &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"]
  htnQ([htn Q]) --> htnP[/htn P/]
end
subgraph asthma-qp["asthma QP &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  &nbsp;"]
  asthmaQ([asthma Q]) --> asthmaP[/asthma P/]
end
subgraph apnea-qp["apnea QP &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"]
  apneaQ([apnea Q]) --> apneaP[/apnea P/]
end

htnP & apneaP & asthmaP -.-> rrdDoneQ

subgraph manual-qp["manual QP &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"]
  manualQ([email inbox]) --> manualP[Manual Process]
end
manualP -.manual.-> rrdDoneQ

subgraph qa-qp[QA QP]
  rrdDoneQ([QA Q]) --> qaP[/QA Processor/]
end

qaP -.-> notifierQ
subgraph notifier-qp[Notifier QP]
  notifierQ([Notifier Q]) --> notifierP[/Notifier Processor/]
end

notifierP -.-> CONTINUE[[downstream claims processing]]

classDef queue fill:#0ee,stroke:#333,stroke-width:4px
class newClaimsQ,manualQ,rrdDoneQ queue
class htnQ,apneaQ,asthmaQ queue
classDef processor fill:#f9f,stroke:#333,stroke-width:2px
class router,qaP,htnP,apneaP,asthmaP,manualP processor
```

- The Router QP responds to RRD API requests and determines how the claim should be processed
    - For each new claim, it quickly validates the claim to send a response back to the API caller.
    - Based on the claim characteristics and any other data it gathers, it determines how the claim should proceed and adds the claim to an appropriate Queue for processing.
- Each hypertension/asthma/apnea QP components is expected to:
    1. receive an item from its Queue (either pushed or pulled)
    2. gathers health data by querying external data sources
    3. decides on the claim (see details in the “RRD processing” section)
    4. adds a new item to the QA Queue
- A Manual QP component can be included for discovery and research of new types of claims to fast track, or for scenarios where a claim has curious characteristics that require manual intervention.
- The QA Processor performs quality assurance; to validate the RRD processing output and prep it for downstream processing (external to RRD). (Not shown in the diagram: another Manual QP component can be added for cases where the QA Processor finds an unsupported problem with the results.)
- If requested as part of the original RRD API request, the Notifier Processor will execute any requested callbacks to indicate completion of RRD processing on the claim.


## Application start-up

When `./gradlew bootRun` is run, the following happens:
- Spring looks for and runs the class with `@SpringBootApplication`, which determines the primary Java package to search for `@Configuration`, `@Component`, `@Service`, and other Spring-annotated classes.
- Spring creates instances based on `@Bean` declarations in order to fulfill `@Autowired` declarations.
- `@Value` variable values are populated from `application.properties` and `application.yml` files.
- The `AppConfig` class performs some set up of the Camel context, while `CamelRestConfiguration`, `RrdApiRoute`, and `ClaimProcessorRoute` set up Camel routes (as described in the "Camel Routes" section).


## Camel Routes

Class `CamelRestConfiguration` sets up the REST endpoint `contextPath` and `/api-doc`.
Class `RrdApiRoute` and `ClaimProcessorRoute` set up Camel routes:
- REST endpoints:
  + doc-api (`rest-api:///api-doc`) - Automatically-generated API docs based on Camel routes
  + rest-POST-claim (`rest://post:/claims:/`) - REST endpoint where new claims are submitted
  + claims-getAll (`rest://get:/claims:/`) - REST endpoint to list all claims
  + claims-getById (`rest://get:/claims:/{id}`) - REST endpoint to get a specific claim
  + claimDetails-getById (`rest://get:/claims:/details/{id}`) - REST endpoint to get a specific claim using different `ClaimService` implementation
  + claim-status-change (`rest://get:/claims:/{id}/status-diff-from/{status}`) - REST endpoint to block until the claim's status changes from the specified status
- occurs as part of the `rest-POST-claim` route:
  + route2 (`seda://addClaim`) - to save submitted Claim to DB and assign a UUID before sending it for processing
  + route1 (`seda://logToFile`) - to save submitted claims to a log file
  + routing-claim (`seda://claim-router`) - to decide how the claim will be processed
- routes to claim processors:
  + seda-claimTypeA (`seda://claimTypeA`) - SEDA route to have the claim processed by processor A (in the same JVM)
  + claimTypeB (`rabbitmq://claimTypeB`) - RabbitMQ route to have the claim processed by processor B (located anywhere that has access to the RabbitMQ service)
  + claimTypeD (`rabbitmq://claimTypeD`) - RabbitMQ route to have the claim processed by processor D (located anywhere that has access to the RabbitMQ service)
- route3 (`seda://claim-rrd-processed-{submission_id}`) - route used by processors to notify that a claim completed processing

Processors:
- `ClaimProcessorA` is written in Java and triggered by a SEDA route
- `ClaimProcessorB` is written in Groovy and triggered by a RabbitMQ route
- `ClaimProcessorD` executes Ruby code within the JVM and triggered by a RabbitMQ route

<!-- - internal REST endpoints
  + route4 (`rest://post:/claim:processA/`) - to process claim of type A via a REST endpoint
  + inject-claim (`rest://get:/claim:processA/`) -
 -->

## Run and Test

For each new console, run `source sourceme.sh` to define convenience functions.

1. In one console, start RabbitMQ: `startRabbitMQ`
2. In another console, start the application: `./gradlew bootRun`
3. In another console, submit requests to the API:
```sh
curlClaims
# (Expect an empty array for the response)
curlPostContention A

curlClaims
# (Note the `status` is `CREATED`)
(wait 5 seconds)
curlClaims
# (Note the `status` is `DONE_RRD`)

curlWaitForStatusChange
# (This blocks until a change occurs, which will be done in the next step)
```
4. In another console, submit requests to the API:
```sh
curlPostContention A
# (Expect console in prior step to return a response with "resultStatus" : "Success")
```
5. To see what an error looks like on the server and client side, try:
```sh
curl -H "Content-Type: application/json" 'http://localhost:8080/camelapp/claim/processA/1234567890'

# To get a successful response, use a UUID from the `curlClaims` call
curl -H "Content-Type: application/json" 'http://localhost:8080/camelapp/claim/processA/eaa3e096-c50f-47f9-8c3b-9180ce4f3236'
```
6. Try the other routes/processors by submitting different a contention:
```sh
curlPostContention A
curlPostContention B
curlPostContention D

# (Expect this to fail since there's no processor C)
curlPostContention C
```
7. Start processor C in another console:
```sh
cd ruby-processor
bundle install

ruby processor_c.rb
```
Then retry contention C:
```sh
curlPostContention C
```

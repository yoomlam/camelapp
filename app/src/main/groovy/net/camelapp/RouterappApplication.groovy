package net.camelapp

import groovy.util.logging.Slf4j
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@Slf4j
class RouterappApplication {

    static void main(args) {
        log.info('Running @SpringBootApplication RouterappApplication')
        SpringApplication.run(RouterappApplication, args)
    }

}

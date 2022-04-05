package net.camelapp

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
class RouterappApplication {

  static void main(String[] args) {
    println("Running @SpringBootApplication RouterappApplication")
    SpringApplication.run(RouterappApplication, args)
  }

}

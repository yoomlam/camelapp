package net.camelapp.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class RouterappApplication {

  static void main(String[] args) {
    println("Running @SpringBootApplication RouterappApplication")
    SpringApplication.run(RouterappApplication, args)
  }

}

package pe.upeu.agrosierra

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AgroApiApplication

fun main(args: Array<String>) {
    runApplication<AgroApiApplication>(*args)
}

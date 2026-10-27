package pe.upeu.agrosierra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class AplicacionConfig {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}

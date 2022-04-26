package com.esg.team.esg

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class EsgApplication

fun main(args: Array<String>) {
    runApplication<EsgApplication>(*args)
}

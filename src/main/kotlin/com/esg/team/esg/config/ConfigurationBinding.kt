package com.esg.team.esg.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class ConfigurationBinding {
    companion object {
        var interfaceName: String = "";
        var interfaceName_r: String = "";
    }

    @ConstructorBinding
    @ConfigurationProperties(prefix = "custom")
    data class Config(
        val network: NetworkProperties,
        val linuxuser: LinuxUserProperties,
        val path: PathProperties,
        val build_option: BuildOptionProperties
    ) {
        data class NetworkProperties(
            var vid: String,
            var ip: String
        )

        data class LinuxUserProperties(
            val app_name: String,
            val version: String,
            val serial: String,
            val passwd: String,
            val release_date: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        )

        data class PathProperties(
            val server_ca_cert_path: String,
            val server_cert_path: String,
            val device_ca_cert_path: String,
            val device_cert_path: String
        )

        data class BuildOptionProperties(
            val debug: Boolean
        )
    }
}


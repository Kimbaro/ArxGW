package com.esg.team.esg.config

import org.springframework.stereotype.Component

@Component
class MqttConfiguration(var conf: ConfigurationBinding) {
    companion object {
        fun mqtt_template3(
            port: Int,
            anonymous: Boolean,
            ssl: Boolean,
            cafile: String = "ca.crt",
            certfile: String = "pi.crt",
            keyfile: String = "pi.key"
        ): String {

            return "# Place your local configuration in /etc/mosquitto/conf.d/\n" +
                    "#\n" +
                    "# A full description of the configuration file is at\n" +
                    "# /usr/share/doc/mosquitto/examples/mosquitto.conf.example\n" +
                    "\n" +
                    "pid_file /var/run/mosquitto.pid\n" +
                    "\n" +
                    "persistence false\n" +
                    "persistence_location /var/lib/mosquitto/\n" +
                    "\n" +
                    "log_dest file /var/log/mosquitto/mosquitto.log\n" +
                    "\n" +
                    "include_dir /etc/mosquitto/conf.d\n" +
                    "# MQTT TLS/SSL\n" +
                    if (ssl) {
                        "listener ${port}\n" +
                                "cafile /etc/mosquitto/device/ca_certificates/${cafile}\n" +
                                "certfile /etc/mosquitto/device/certs/${certfile}\n" +
                                "keyfile /etc/mosquitto/device/certs/${keyfile}\n" +
                                "tls_version tlsv1.2\n" +
                                "require_certificate ${ssl}\n"
                    } else {
                        "listener ${port}\n" +
                                "protocol mqtt\n" +
                                "\n"
                    } +
                    "\n" +
                    "# MQTT Login\n" +
                    if (anonymous) {
                        ""
                    } else {
                        "allow_anonymous ${anonymous}\n" +
                                "password_file /etc/mosquitto/passwd\n"
                    }
        }

    }
}
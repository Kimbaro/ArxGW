package com.esg.team.esg.controller

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.domain.Certificate
import com.esg.team.esg.domain.Certificate_Save
import com.esg.team.esg.service.SslConfigurationIO
import com.esg.team.esg.service.utils.CommandLineInterface
import com.esg.team.esg.service.utils.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * MQTT의 SSL 통신을 위한 인증서 생성,검증을 담당함
 * */
@RestController
@RequestMapping("/raspgw/cert")
class CertAPI(var conf: ConfigurationBinding.Config, val cmdLine: CommandLineInterface) {

    @Autowired
    var sslConfigurationIO: SslConfigurationIO = SslConfigurationIO(conf, cmdLine);

    @Autowired
    var dataManager: DataManager = DataManager();

    @RequestMapping(method = [RequestMethod.GET])
    fun readCert(): Map<String, Certificate> {
        var ca_cert: Array<String> = sslConfigurationIO.bufferdFileRead(sslConfigurationIO.device_ca_cert.toFile());
        var public_cert: Array<String> = sslConfigurationIO.bufferdFileRead(sslConfigurationIO.device_cert.toFile())
        var public_cert_key: Array<String> = sslConfigurationIO.bufferdFileRead(sslConfigurationIO.device_key.toFile())
        var merge_cert: Certificate = Certificate(
            ca_cert, public_cert, public_cert_key
        );
        return mapOf("data" to merge_cert);
    }

    @RequestMapping(method = [RequestMethod.POST])
    fun refreshCert(@RequestBody certificate: Certificate_Save): ResponseEntity<Any> {
        println(certificate.cacert);
        println(certificate.devicecert);
        println(certificate.devicekey);

        sslConfigurationIO.refreshDeviceSSLFile(certificate);
        return ResponseEntity.ok().body(true);
    }
}
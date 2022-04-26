package com.esg.team.esg.controller

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.domain.Mqtt
import com.esg.team.esg.service.MqttConfigurationIO
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
 * Mosquitto의 설정/제어를 담당함
 * */
@RestController
@RequestMapping("/raspgw/broker")
class MqttAPI(var conf: ConfigurationBinding.Config, val cmdLine: CommandLineInterface) {
    @Autowired
    var mqttConfigurationIO: MqttConfigurationIO = MqttConfigurationIO(conf, cmdLine);

    @Autowired
    var dataManager: DataManager = DataManager();


    @RequestMapping(method = [RequestMethod.GET])
    fun readBroker(): Map<String, Mqtt> {
        var mqtt: Mqtt = mqttConfigurationIO.readData()
        return mapOf("data" to mqtt);
    }

    @RequestMapping(method = [RequestMethod.POST])
    fun craeteBroker(@RequestBody mqtt: Mqtt): ResponseEntity<Any> {
        mqttConfigurationIO.refreshMosquittoConf(mqtt);
        return ResponseEntity.ok().body(true);
    }
}
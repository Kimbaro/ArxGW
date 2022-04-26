package com.esg.team.esg.controller

import com.esg.team.esg.config.ConfigurationBinding
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Front-End의 Info 부분을 담당함
 * */
@RestController
@RequestMapping("/raspgw/info")
class InfoAPI(var conf: ConfigurationBinding.Config) {

    @RequestMapping(method = [RequestMethod.GET])
    fun getInfo(): Map<String, Map<String, String>> {
        var data = mapOf(
            "data" to mapOf(
                "app.name" to conf.linuxuser.app_name,
                "release.date" to conf.linuxuser.release_date,
                "release.version" to conf.linuxuser.version,
                "serial.number" to conf.linuxuser.serial
            )
        );
        return data;
    }
}
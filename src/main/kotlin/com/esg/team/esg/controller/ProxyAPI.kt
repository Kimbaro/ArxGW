package com.esg.team.esg.controller

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.domain.Proxy
import com.esg.team.esg.service.ProxyConfigurationIO
import com.esg.team.esg.service.utils.Antilogarithm
import com.esg.team.esg.service.utils.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * TODO
 * Keepalived와 Haproxy의 설정,제어를 담당함
 * keepalived, haproxy 여부를 선택하고 해당 정보를 OS에서 반영하도록 해야함.
 * */
@RestController
@RequestMapping("/raspgw/mode")
class ProxyAPI(val conf: ConfigurationBinding.Config, antilogarithm: Antilogarithm) {

    @Autowired
    var proxyConfigurationIO: ProxyConfigurationIO = ProxyConfigurationIO(conf, antilogarithm);

    @Autowired
    var dataManager: DataManager = DataManager();


    @RequestMapping(method = [RequestMethod.GET])
    fun readProxy(): Map<String, Proxy> {
        return mapOf("data" to proxyConfigurationIO.readData());
    }

    @RequestMapping(method = [RequestMethod.POST])
    fun craeteProxy(@RequestBody proxy: Proxy): ResponseEntity<Any> {
        proxyConfigurationIO.readFile();
        proxyConfigurationIO.refreshNetworkFile(proxy);
        return ResponseEntity.ok().body(true);
    }
}
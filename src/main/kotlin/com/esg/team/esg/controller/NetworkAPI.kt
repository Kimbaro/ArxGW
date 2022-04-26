package com.esg.team.esg.controller

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.domain.Network
import com.esg.team.esg.service.NetworkConfigurationIO_Raspberry
import com.esg.team.esg.service.utils.Antilogarithm
import com.esg.team.esg.service.utils.CommandLineInterface
import com.esg.team.esg.service.utils.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 네트워크 설정 정보를 변경함
 * 우분투내 네트워크 파일 시스템에 접근하여 정보를 변경하거나 가져옴
 *  - DHCP ON/OFF 에 따라 netplan의 네트워크 정보에 쓰게되는 정보가 다름
 * */
@RestController
@RequestMapping("/raspgw/network")
class NetworkAPI(
    val conf: ConfigurationBinding.Config,
    val antilogarithm: Antilogarithm,
    var cmdLine: CommandLineInterface
) {

    @Autowired
    var io_r: NetworkConfigurationIO_Raspberry = NetworkConfigurationIO_Raspberry(conf, antilogarithm, cmdLine); //라즈베리

    @Autowired
    var dataManager: DataManager = DataManager();


    @RequestMapping(method = [RequestMethod.GET])
    fun readNetwork_r(): Map<String, Network> {
        //local server의 netplan 정보를 read하고 클라이언트에게 반환합니다.
        io_r.readFile();
        val network: Network = io_r.readData();
        return mapOf("data" to network);
    }


    @RequestMapping(method = [RequestMethod.POST])
    fun saveNetwork_r(@RequestBody network: Network): ResponseEntity<Any> {
        io_r.refreshNetworkFile(network)
        println(network);
        return ResponseEntity.ok().body(true);
    }
}
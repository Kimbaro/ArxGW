package com.esg.team.esg.domain

/**
 * @param gateway : true = gateway(keepalived), false = switch(haproxy)
 * @param vip : virtual-ip = 가상 proxy의 ip
 * @param subnet : vip의 subnet-mask
 * @param state : true = Master, false = Slave
 * */
data class Proxy(val mode: Boolean = true, val vip: String = "", val subnet: String = "", val state: Boolean = true) {

}
package com.esg.team.esg.domain


data class Network(
    val dhcp: Boolean = true,
    val ip: String? = null,
    val gateway: String? = null,
    val dns: String? = null,
    val subnet: String? = null
)

data class Network_List(var data: List<Network>)
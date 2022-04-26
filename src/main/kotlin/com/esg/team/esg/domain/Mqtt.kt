package com.esg.team.esg.domain

import com.esg.team.esg.config.ConfigurationBinding

//변수이름 동일하게
data class Mqtt(
    var enable: Boolean = false,
    var ip: String ="",
    var port: Int = -1,
    var topic: String = "",
    var anonymous: Boolean = false,
    var ssl: Boolean = false,
    var id: String = "",
    var password: String = ""
)
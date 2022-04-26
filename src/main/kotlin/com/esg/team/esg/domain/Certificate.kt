package com.esg.team.esg.domain

data class Certificate(var cacert: Array<String>, var devicecert: Array<String>, var devicekey: Array<String>)

data class Certificate_Save(var cacert: String, var devicecert: String, var devicekey: String)

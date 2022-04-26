package com.esg.team.esg.service.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Path

@Component
class DataManager {
    private var filePath: Path = Path.of("/etc/mosquitto");
    private var adminAccountFileName: String = "testPasswd";
    private var file: File = File(filePath.toString() + "/" + adminAccountFileName);

    var data: Data = Data()

    //기존 파일을 읽어서 데이터를 가져옵니다
    fun init() {
        if (!file.exists()) {
            file.createNewFile() //파일이 존재하지 않는 경우 생성
        }
        var linecount: Int = 1;
        file.forEachLine {
            when (linecount) {
                1 -> {
                    data.key = it;
                }
                2 -> {
                    data.iv = it;
                }
                3 -> {
                    data.id = it;
                }
                4 -> {
                    data.pw = it;
                }
                5 -> {
                    data.ssl = it;
                }
                6 -> {
                    data.port = it
                }
                7 -> {
                    data.vip = it
                }
            }
            linecount++;
        }
    }

    fun save() {
        file.delete().apply {
            if (!file.exists()) {
                file.createNewFile() //파일이 존재하지 않는 경우 생성
            }
            file.printWriter().use {
                it.println(data.key)
                it.println(data.iv)
                it.println(data.id)
                it.println(data.pw)
                it.println(data.ssl)
                it.println(data.port)
                it.println(data.vip)
            }
        }
    }

    data class Data(
        var key: String = "",
        var iv: String = "",
        var id: String = "",
        var pw: String = "",
        var ssl: String = "",
        var port: String = "",
        var vip: String = ""
    )
}
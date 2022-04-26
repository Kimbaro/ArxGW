package com.esg.team.esg.service.utils

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.domain.Proxy
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Path

@Component
class CommandLineInterface(val conf: ConfigurationBinding.Config) {
    //TODO 파일시스템에 명령을 보내기위한 커맨드라인
    var cmd: Array<String>? = null;
    var process: Process? = null;
    fun get(command: String, param1: String = "", param2: String = "", param3: String = "") {
        when (command) {
            "mosquitto_passwd" -> {
                cmd = arrayOf(
                    "/bin/bash",
                    "-c",
                    "echo ${conf.linuxuser.passwd} | sudo -S mosquitto_passwd -b ${param1} ${param2} ${param3}"
                );
            }
            "start mosquitto.service" -> {
                cmd = arrayOf(
                    "/bin/bash",
                    "-c",
                    "echo ${conf.linuxuser.passwd} | sudo -S systemctl start mosquitto.service"
                );
            }
            "stop mosquitto.service" -> {
                cmd =
                    arrayOf(
                        "/bin/bash", "-c",
                        "echo ${conf.linuxuser.passwd} | sudo -S systemctl stop mosquitto.service"
                    );
            }
            "chmod 770 /etc/dhcpcd.conf" -> {
                cmd = arrayOf("/bin/bash", "-c", "echo ${conf.linuxuser.passwd} | sudo -S chmod 770 /etc/dhcpcd.conf");
            }
            "cat /proc/net/dev" -> {
                cmd = arrayOf("/bin/bash", "-c", "cat /proc/net/dev");
            }
            "/etc/init.d/networking restart" -> {
                cmd =
                    arrayOf(
                        "/bin/bash", "-c",
                        "echo ${conf.linuxuser.passwd} | sudo -S /etc/init.d/networking restart"
                    );
            }
            "sudo -S reboot" -> {
                cmd = arrayOf("/bin/bash", "-c", "echo ${conf.linuxuser.passwd} | sudo -S reboot");
            }
            "chmod 770 /etc/keepalived/keepalived.conf" -> {
                cmd = arrayOf(
                    "/bin/bash",
                    "-c",
                    "echo ${conf.linuxuser.passwd} | sudo -S chmod 770 /etc/keepalived/keepalived.conf"
                );
            }
            "systemctl stop keepalived.service" -> {
                cmd =
                    arrayOf(
                        "/bin/bash", "-c",
                        "echo ${conf.linuxuser.passwd} | sudo -S systemctl stop keepalived.service"
                    );
            }
            "systemctl start keepalived.service" -> {
                cmd = arrayOf(
                    "/bin/bash",
                    "-c",
                    "echo ${conf.linuxuser.passwd} | sudo -S systemctl start keepalived.service"
                );
            }
        }
        process = Runtime.getRuntime().exec(cmd);
        bufferRead(process, cmd);
    }

    fun bufferRead(process: Process?, command: Array<String>?) {
        val stdInput = BufferedReader(InputStreamReader(process!!.inputStream))
        //val stdError = BufferedReader(InputStreamReader(process!!.errorStream))

        if (command != null) {
            println(command.asList().toString())
            var s: String? = null
            while (stdInput.readLine().also { s = it } != null) {
                println(s)
            }
/*            while (stdError.readLine().also { s = it } != null) {
                println(s)
            }*/
            process.waitFor();
        };
    }

    fun searchPort(): Int {
        var filePath: Path = Path.of("/etc/mosquitto");
        var defaultFileName: String = "mosquitto.conf";
        var file: File =
            File(filePath.toString() + "/" + defaultFileName);
        var port: Int = 1883;
        file.forEachLine {
            when {
                it.contains("listener") -> {
                    port = it.replace("listener", "").replace(" ", "").toInt();
                }
            }
        }
        return port
    }

    fun searchIP(): String {
        var cmd = arrayOf("/bin/bash", "-c", "sudo -S ifconfig | grep eth0 -A 1");
        var process: Process = Runtime.getRuntime().exec(cmd);
        val stdInput = BufferedReader(InputStreamReader(process!!.inputStream))
        var s: String = ""
        while (stdInput.readLine().also { s = it } != null) {
            if (s.contains("inet")) {
                return s.split(" ").get(9);
            }
        }
        return "127.0.0.1"
    }

    //keepalived ip를 반환
    fun searchVirtualIP(): String {
        var filePath: Path = Path.of("/etc/keepalived/");
        var defaultFileName: String = "keepalived.conf";
        var file: File =
            File(filePath.toString() + "/" + defaultFileName);
        if (file.exists()) {
            var gateway: Boolean = true
            var vip: String = "";

            //파일이 있는 경우 읽어옵니다.
            file.forEachLine {
                when {
                    it.contains("virtual_ipaddress") -> {
                        var value: String =
                            it.replace("virtual_ipaddress", "")
                                .replace(" ", "")
                                .replace("{", "")
                                .replace("}", "")
                        var values: List<String> = value.split("/");
                        vip = values.get(0);
                        //subnet = decimalToSubnetmask(values.get(1).toInt());
                    }
                }
            }
            return vip;
        } else {
            return searchIP();
        }
    }
}
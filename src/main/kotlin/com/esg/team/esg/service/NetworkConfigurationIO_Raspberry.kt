package com.esg.team.esg.service

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.config.DhcpConfiguration
import com.esg.team.esg.domain.Network
import com.esg.team.esg.service.utils.Antilogarithm
import com.esg.team.esg.service.utils.CommandLineInterface
import com.esg.team.esg.service.utils.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Path
import javax.annotation.PostConstruct

@Service
class NetworkConfigurationIO_Raspberry(
    val conf: ConfigurationBinding.Config,
    val antilogarithm: Antilogarithm,
    var cmdLine: CommandLineInterface
) {
    var filePath: Path = Path.of("/etc/");
    var defaultFileName: String = "/dhcpcd.conf";
    var oldFileName: String = "/dhcpcd.old";
    var interfaceName: String = "???";

    @PostConstruct
    fun systemSetup() {
        CoroutineScope(Dispatchers.IO).async {
            cmdLine.get("chmod 770 /etc/dhcpcd.conf")
            interfaceName = searchInterfaceName();
            ConfigurationBinding.interfaceName_r = interfaceName;
            conf.network.ip = cmdLine.searchIP()
        }
    }

    //TODO dhcpcd.conf 파일 읽기
    fun readFile() {
        var dir: File =
            File(filePath.toString());
        var fileVerification: Boolean = false;
        if (dir.listFiles().size > 0) {
            for (file in dir.listFiles()) {
                if (file.name.equals("dhcpcd.conf")) {
                    defaultFileName = "/" + file.getName();
                    fileVerification = true;
                }
            }
        }
        if (!fileVerification) { //파일이 존재하지 않음
            craeteInitNetworkFile();
        }
    }

    //TODO dhcpd.conf 파일의 경로 반환
    fun searchFileReturnPath(fileName: String): String? {
        var dir: File =
            File(filePath.toString());
        if (dir.listFiles().size > 0) {
            for (file in dir.listFiles()) {
                if (file.name.equals("${fileName}")) {// 파일이 존재함
                    return filePath.toString() + "/" + file.getName();
                }
            }
        }
        //파일이 존재하지 않음
        return null;
    }


    //TODO 네트워크 인터페이스 이름 가져오기
    fun searchInterfaceName(): String {
        var cmd = arrayOf("/bin/bash", "-c", "cat /proc/net/dev");
        var process: Process = Runtime.getRuntime().exec(cmd);
        var interfaceName: String = "???";
        process.inputStream.bufferedReader().forEachLine {
            if (it.replace(" ", "").get(0) == 'e') {
                interfaceName = it.split(":").get(0).replace(" ", "");
            }
        }
        return interfaceName;
    }

    //TODO 10진수를 서브넷마스크로 24 -> 255.255.255.0
    fun decimalToSubnetmask(decimal: Int): String {
        var subnet_temp: String = "";
        for (i in 1..decimal) {
            subnet_temp += "1";
        }
        for (i in subnet_temp.length + 1..32) {
            subnet_temp += "0";
        }

        var binaryToDecimal: String = "";
        var decimalToSubnetmask: String = "";

        for (i in 0..subnet_temp.length - 1) {
            binaryToDecimal += subnet_temp.get(i);
            if (binaryToDecimal.length % 8 == 0) {
                println(Integer.parseInt(binaryToDecimal, 2));
                decimalToSubnetmask += Integer.parseInt(binaryToDecimal, 2).toString() + ".";
                binaryToDecimal = "";
            }
        }
        decimalToSubnetmask = decimalToSubnetmask.substring(0, decimalToSubnetmask.length - 1);
        return decimalToSubnetmask;
    }

    //TODO dhcpcd.conf 파일을 읽고 파싱하여 반환
    fun readData(): Network {
        var file: File =
            File(filePath.toString() + defaultFileName);
        return parsingData(file);
        //file.inputStream().bufferedReader().use { 여기서부터 한줄씩 읽어서 처리하도록 해야함. }
    }

    //readData() 이후 Text 값을 파싱하여 Json 형태로 반환합니다.
    fun parsingData(file: File): Network {
        var dhcp: Boolean = true;
        var ip: String = "";
        var gateway: String = "";
        var dns: String = "";
        var subnet: String = "";
        var network: Network = Network();
        file.forEachLine {
            var data: List<String> = it.split("=");
            if (data.isNotEmpty() && data.size == 2) {
                when (data.get(0)) {
                    "#dhcp" -> dhcp = data.get(1).toBoolean();
                    "static ip_address" -> ip = data.get(1);
                    "static routers" -> gateway = data.get(1);
                    "static domain_name_servers" -> dns = data.get(1);
                }
            }
        }
        if (dhcp) {
            network = Network();
        } else {
            var splits: List<String> = ip.split("/");
            ip = splits.get(0);
            subnet = antilogarithm.decimalToSubnetmask(splits.get(1).toInt())
            //subnet = decimalToSubnetmask(splits.get(1).toInt())
            network = Network(
                dhcp,
                ip.split("/").get(0).replace(" ", ""),
                gateway,
                dns,
                subnet
            );
        }
        return network;
    }

    //TODO 클라이언트가 신규 네트워크 정보를 save 한 경우 새로 갱신
    fun refreshNetworkFile(network: Network) {
        var oldFilepath: String? = searchFileReturnPath("dhcpcd.old");
        var filePath: String? = searchFileReturnPath("dhcpcd.conf");
        println(oldFilepath)
        println(filePath)

        var file: File;
        if (oldFilepath != null) {
            //파일삭제
            file = File(oldFilepath);
            file.deleteOnExit();
        }
        if (filePath != null) {
            //old 변환
            file = File(filePath);
            file.renameTo(File(this.filePath.toString() + this.oldFileName));
        }
        file = File(filePath);
        file.createNewFile();
        if (network.dhcp) {
            file.printWriter().use { it.println(DhcpConfiguration.r_dhcp_on(interfaceName)) }
        } else {
            file.printWriter().use {
                it.println(
                    DhcpConfiguration.r_dhcp_off(
                        interfaceName,
                        network.ip,
                        antilogarithm.subnetmaskToDecimal(network.subnet).toString(),
//                        subnetmaskToDecimal(network.subnet).toString(),
                        network.gateway,
                        network.dns
                    )
                )
            }
        }
        try {
            //cmdLine.get("/etc/init.d/networking restart")
            cmdLine.get("sudo -S reboot")
        } catch (e: Exception) {
            e.printStackTrace();
        };
    }

    //TODO 만일, netplan 하위 디렉토리에 dhcpcd.conf 존재하지 않으면 새로 생성한다
    fun craeteInitNetworkFile() {
        var dir: File = File(filePath.toString() + defaultFileName);
        println(dir.path);
        if (dir.createNewFile()) {
            dir.setWritable(true);
            dir.setReadable(true);
            File(filePath.toString() + defaultFileName)
                .printWriter()
                .use { it.println(DhcpConfiguration.r_dhcp_on(ConfigurationBinding.interfaceName_r)) }
        }
    }
}
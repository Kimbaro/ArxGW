package com.esg.team.esg.service

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.config.ProxyConfiguration
import com.esg.team.esg.domain.Proxy
import com.esg.team.esg.service.utils.Antilogarithm
import com.esg.team.esg.service.utils.CommandLineInterface
import com.esg.team.esg.service.utils.DataManager
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import javax.annotation.PostConstruct

@Service
class ProxyConfigurationIO(
    val conf: ConfigurationBinding.Config,
    val antilogarithm: Antilogarithm,
) {
    var filePath: Path = Path.of("/etc/keepalived/");
    var defaultFileName: String = "keepalived.conf";
    var oldFileName: String = "keepalived.old";
    var interfaceName: String = "???";

    @Autowired
    var cmdLine: CommandLineInterface = CommandLineInterface(conf);

    @Autowired
    var dataManager: DataManager = DataManager();

    @PostConstruct
    fun systemSetup() {
        CoroutineScope(Dispatchers.IO).async {
            interfaceName = searchInterfaceName();
            ConfigurationBinding.interfaceName_r = interfaceName;
            var file: File =
                File(filePath.toString() + "/" + defaultFileName);
            if (!file.exists()) {
                createKeepalivedFile(file)
            }
            cmdLine.get("chmod 644 /etc/keepalived/keepalived.conf")
        }
    }

    //TODO 네트워크 인터페이스 명을 가져옴
    fun searchInterfaceName(): String {
        //cmdLine.get("cat /proc/net/dev")
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

    //TODO keepalived.conf 파일의 경로 반환
    fun searchFileReturnPath(fileName: String): String? {
        var dir: File =
            File(filePath.toString());
        if (dir.listFiles().size > 0) {
            for (file in dir.listFiles()) {
                if (file.name.equals("${fileName}")) {
                    return filePath.toString() + "/" + file.getName();
                }
            }
        }
        return null;
    }

    //TODO keepalived.conf 파일 읽기
    fun readData(): Proxy {
        var file: File =
            File(filePath.toString() + "/" + defaultFileName);
        return parsingData(file);
    }

    //TODO readData() 이후 Text 값을 파싱하여 Json 형태로 반환
    fun parsingData(file: File): Proxy {
        var gateway: Boolean = true
        var vip: String = "?.?.?.?";
        var subnet: String = "8"
        var state: Boolean = true
        var result: Proxy = Proxy();
        if (file.exists()) {
            when (gateway) {
                //Keepalived
                true -> {
                    //파일이 있는 경우 읽어옵니다.
                    file.forEachLine {
                        when {
                            it.contains("state") -> {
                                if (it.replace("state", "").replace(" ", "").equals("MASTER")) {
                                    state = true
                                } else {
                                    state = false
                                }
                            }
                            it.contains("interface") -> {
                                println(it.replace("interface", "").replace(" ", ""));
                            }
                            it.contains("virtual_ipaddress") -> {
                                var value: String =
                                    it.replace("virtual_ipaddress", "")
                                        .replace(" ", "")
                                        .replace("{", "")
                                        .replace("}", "")
                                var values: List<String> = value.split("/");
                                vip = values.get(0);
                                subnet = antilogarithm.decimalToSubnetmask(values.get(1).toInt());
                                CoroutineScope(Dispatchers.IO).async {
                                    dataManager.init()
                                    dataManager.data.vip = vip;
                                    dataManager.save()
                                }
                                //subnet = decimalToSubnetmask(values.get(1).toInt());
                            }
                        }
                    }
                    result = Proxy(gateway, vip, subnet, state);
                }
                false -> {
                    //TODO Haproxy 구현부,
                }
            }
            return result;
        } else {
            return Proxy(true, vip, antilogarithm.decimalToSubnetmask(subnet.toInt()), true);
        }
    }

    //TODO keepalived 설정파일이 존재하는지 확인, 존재하지 않는 경우 생성.
    fun readFile() {
        var dir: File =
            File(filePath.toString());
        var fileVerification: Boolean = false;
        if (dir.listFiles().size > 0) {
            for (file in dir.listFiles()) {
                if (file.name.equals("keepalived.conf")) {
                    println("파일이 존재함." + file.name);
                    defaultFileName = "/" + file.getName();
                    fileVerification = true;
                }
            }
        }
        if (!fileVerification) {
            println("파일이 존재하지 않음");
            createKeepalivedFile(dir);
        }
    }

    fun createKeepalivedFile(file: File): Proxy {
        var defaultMode: Boolean = true;
        var defaultVip: String = "파일이.존재하.지않습.니다";
        var defaultSubnet: String = "새로.입력후.저장해.주세요";
        var defaultState: Boolean = true;

        file.printWriter()
            .use {
                it.println(
                    ProxyConfiguration.keepalived_template(
                        interfaceName, "127.0.0.1", "8", "MASTER", conf.network.vid
                    )
                )
            }
            .apply {
                println("완료");
            }
        return Proxy(defaultMode, defaultVip, defaultSubnet, defaultState);
    }

    //TODO 클라이언트가 신규 keepalived 정보를 보낸 경우 새로 갱신
    fun refreshNetworkFile(proxy: Proxy) {
        var oldFilepath: String? = searchFileReturnPath(this.oldFileName);
        var filePath: String? = searchFileReturnPath("keepalived.conf");
        println(oldFilepath)
        println(filePath)

        var file: File;
        if (oldFilepath != null) {
            //파일삭제
            file = File(oldFilepath);
            file.deleteOnExit();
        }
        if (filePath != null && oldFilepath != null) {
            //old 변환
            file = File(filePath);
            file.renameTo(File(oldFilepath));
        } else if (filePath != null) {
            file = File(filePath);
            file.renameTo(File(this.filePath.toString() + "/" + oldFileName));
        }
        file = File(filePath);
        file.createNewFile();
        cmdLine.get("chmod 644 /etc/keepalived/keepalived.conf")
        if (proxy.mode) { //keepalived
            var subnet: String = antilogarithm.subnetmaskToDecimal(proxy.subnet).toString()
            file.printWriter()
                .use {
                    it.println(
                        ProxyConfiguration.keepalived_template(
                            interfaceName,
                            proxy.vip,
                            subnet,
                            if (proxy.state) {
                                "MASTER"
                            } else {
                                "BACKUP"
                            },
                            conf.network.vid
                        )
                    )
                }
        } else {
            //TODO haproxy 구현부
        }
        try {
            cmdLine.get("systemctl stop keepalived.service")

            var scope = CoroutineScope(Dispatchers.IO);
            scope.async {
                delay(1000L)
                cmdLine.get("systemctl start keepalived.service")
                initData(proxy.vip)
                dataManager.init()
                dataManager.data.vip = proxy.vip;
                dataManager.save()
                //plcDriverConfigurationIO.refreshPlcConfFile()
            }
        } catch (e: Exception) {
            e.printStackTrace();
        };
    }

    //가상아이피 변경에 따라 server 인증서 또한 새로 발급합니다.
    fun initData(vip: String) {
        val extPath: Path = Path.of(conf.path.server_cert_path + "/test.ext");
        val ca_key: Path = Path.of(conf.path.server_ca_cert_path + "/ca.key");
        val ca_cert: Path = Path.of(conf.path.server_ca_cert_path + "/ca.crt");

        val server_cert: Path = Path.of(conf.path.server_cert_path + "/pi.crt");
        val server_key: Path = Path.of(conf.path.server_cert_path + "/pi.key");
        val server_csr: Path = Path.of(conf.path.server_cert_path + "/pi.csr");
        CoroutineScope(Dispatchers.IO).async {
            var dir = File("${conf.path.server_ca_cert_path}");
            Files.delete(ca_key);
            Files.delete(ca_cert);
            if (dir.mkdirs()) {
                Files.createFile(ca_key);
                Files.createFile(ca_cert);
                dir.setReadable(true);
                dir.setWritable(true);
            }

            dir = File("${conf.path.server_cert_path}");
            Files.delete(server_cert);
            Files.delete(server_key);
            Files.delete(server_csr);
            if (dir.mkdirs()) {
                Files.createFile(server_cert);
                Files.createFile(server_key);
                Files.createFile(server_csr);
                dir.setReadable(true);
                dir.setWritable(true);
            }

            dir = File("${extPath}");
            dir.delete()
            dir.createNewFile();
            dir.setReadable(true);
            dir.setWritable(true);
            dir.printWriter()
                .use {
                    it.println(
                        "subjectAltName=IP:${vip}"
                    )
                }
            //openssl 커맨드 입력 가이드 https://www.madboa.com/geek/openssl/
            var cmd =
                arrayOf(
                    "/bin/bash", "-c",
                    "echo ${conf.linuxuser.passwd} | sudo -S openssl genrsa -out ${ca_key} 2048"
                );
            var process: Process = Runtime.getRuntime().exec(cmd)
            bufferRead(process, cmd);

            cmd =
                arrayOf(
                    "/bin/bash",
                    "-c",
                    "echo ${conf.linuxuser.passwd} | sudo openssl req -new -x509 -days 365 -key ${ca_key} -out ${ca_cert} -subj '/C=/ST=/L=/CN=aerix'"
                );
            process = Runtime.getRuntime().exec(cmd)
            bufferRead(process, cmd);
            cmd =
                arrayOf(
                    "/bin/bash",
                    "-c",
                    "echo ${conf.linuxuser.passwd} | sudo -S openssl req -new -newkey rsa:2048 -nodes -keyout ${server_key} -out ${server_csr} -subj '/C=/ST=/L=/CN=${cmdLine.searchVirtualIP()}'"
                );
            process = Runtime.getRuntime().exec(cmd)
            bufferRead(process, cmd);

            cmd =
                arrayOf(
                    "/bin/bash",
                    "-c",
                    "echo ${conf.linuxuser.passwd} | sudo -S openssl x509 -req -in ${server_csr} -extfile ${extPath} -CA ${ca_cert} -CAkey ${ca_key} -CAcreateserial -out ${server_cert} -days 365"
                );
            process = Runtime.getRuntime().exec(cmd)
            bufferRead(process, cmd);

            cmd =
                arrayOf(
                    "/bin/bash",
                    "-c",
                    "echo ${conf.linuxuser.passwd} | sudo -S openssl verify -CAfile ${ca_cert} ${server_cert}"
                );
            process = Runtime.getRuntime().exec(cmd)
            bufferRead(process, cmd);
            cmd = arrayOf(
                "/bin/bash",
                "-c",
                "echo ${conf.linuxuser.passwd} | sudo -S openssl verify -CAfile ${ca_cert} ${server_cert}"
            );
            process = Runtime.getRuntime().exec(cmd)
            bufferRead(process, cmd);
        }
    }

    fun bufferRead(process: Process, command: Array<String>) {
        val stdInput = BufferedReader(InputStreamReader(process.inputStream))
        val stdError = BufferedReader(InputStreamReader(process.errorStream))

        println(command.asList().toString());
        var s: String? = null
/*        while (stdInput.readLine().also { s = it } != null) {
            println(s)
        }
        while (stdError.readLine().also { s = it } != null) {
            println(s)
        }*/
        process.waitFor();
    }

}

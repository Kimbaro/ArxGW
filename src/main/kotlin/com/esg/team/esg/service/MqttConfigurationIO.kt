package com.esg.team.esg.service

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.config.MqttConfiguration
import com.esg.team.esg.domain.Mqtt
import com.esg.team.esg.service.utils.CommandLineInterface
import com.esg.team.esg.service.utils.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path
import javax.annotation.PostConstruct
import javax.xml.crypto.Data

@Service
class MqttConfigurationIO(
    val conf: ConfigurationBinding.Config,
    var cmdLine: CommandLineInterface
) {
    var filePath: Path = Path.of("/etc/mosquitto");
    var defaultFileName: String = "mosquitto.conf";
    var passwdFileName: String = "passwd";

    @Autowired
    var cryptionConfigurationIO: CryptionConfigurationIO = CryptionConfigurationIO();
    var dataManager: DataManager = DataManager();

    @PostConstruct
    fun systemSetup() {
        //TODO Mqtt 설정파일 및 패스워드 파일이 존재하는지 검사
        CoroutineScope(Dispatchers.IO).async {
            var dir: File =
                File("/etc/mosquitto/passwd");

            if (!dir.exists()) {
                dir.createNewFile()
                dir.setWritable(true);
                dir.setReadable(true);
            }
            dir = File(filePath.toString() + "/" + defaultFileName);
            if (!dir.exists()) {
                dir.createNewFile()
                dir.setWritable(true);
                dir.setReadable(true);
                refreshMosquittoConf(Mqtt(true, cmdLine.searchIP(), 1883, "test", true, false));
            }
        }
    }

    //TODO mosquitto.conf 파일을 읽고 파싱 후 반환
    fun readData(): Mqtt {
        var file: File =
            File(filePath.toString() + "/" + defaultFileName);
        var file_passwd: File = File(filePath.toString() + "/" + passwdFileName);
        return parsingData(file, file_passwd);
    }

    fun parsingData(file: File, file_passwd: File): Mqtt {
        var port: Int = 1883
        var ssl: Boolean = false
        var anonymous: Boolean = true
        var id: String = ""

        file.forEachLine {
            when {
                it.contains("listener") -> {
                    port = it.replace("listener", "").replace(" ", "").toInt();
                }
                it.contains("require_certificate") -> {
                    ssl = it.replace("require_certificate", "").replace(" ", "").toBoolean();
                }
                it.contains("allow_anonymous") -> {
                    anonymous = it.replace("allow_anonymous", "").replace(" ", "").toBoolean();
                }
            }
        }

        //익명접속 비허용인 경우 passwd 파일에 정의된 id값을 가져옴
        if (anonymous) else file_passwd.forEachLine { id = it.split(":").get(0); }
        var mqtt: Mqtt =
            Mqtt(ssl = ssl, port = port, anonymous = anonymous, id = id, ip = conf.network.ip, password = "리비안주식떡상");
        return mqtt;
    }

    fun refreshMosquittoConf(mqtt: Mqtt) {
        var file: File = File(filePath.toString() + "/" + defaultFileName);
        var file_passwd: File = File(filePath.toString() + "/" + passwdFileName);
        if (mqtt.anonymous) else {
            cmdLine.get(
                "CommandLineInterface.get : " + cmdLine.get(
                    "mosquitto_passwd",
                    file_passwd.path,
                    mqtt.id,
                    mqtt.password
                )
            )
            dataManager.init();
            dataManager.data.key = cryptionConfigurationIO.SECRET_KEY;
            dataManager.data.iv = cryptionConfigurationIO.SECRET_IV;
            dataManager.data.port = mqtt.port.toString();
            dataManager.data.ssl = mqtt.ssl.toString();
            if (mqtt.id.equals("admin")) {  //id 가 admin 인 경우 testPasswd 에도 암호화하여 새로 반영합니다.
                dataManager.data.id = mqtt.id;
                dataManager.data.pw = cryptionConfigurationIO.encrypt(mqtt.password)
            }
            dataManager.save()
        }

        file.createNewFile();
        file.printWriter()
            .use {
                it.println(
                    MqttConfiguration.mqtt_template3(mqtt.port, mqtt.anonymous, mqtt.ssl)
                )
            }

        try {
            cmdLine.get("stop mosquitto.service");

            var scope = CoroutineScope(Dispatchers.IO);
            scope.async {
                delay(1000L)
                cmdLine.get("start mosquitto.service");
                //plcDriverConfigurationIO.refreshPlcConfFile()
            }
        } catch (e: Exception) {
            e.printStackTrace();
        };
    }


}
package com.esg.team.esg.service

import com.esg.team.esg.config.ConfigurationBinding
import com.esg.team.esg.domain.Certificate_Save
import com.esg.team.esg.service.utils.CommandLineInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import javax.annotation.PostConstruct

@Service
class SslConfigurationIO(
    val conf: ConfigurationBinding.Config,
    val cmdLine: CommandLineInterface
) {
    val extPath: Path = Path.of(conf.path.server_cert_path + "/test.ext");
    val ca_key: Path = Path.of(conf.path.server_ca_cert_path + "/ca.key");
    val ca_cert: Path = Path.of(conf.path.server_ca_cert_path + "/ca.crt");

    val server_cert: Path = Path.of(conf.path.server_cert_path + "/pi.crt");
    val server_key: Path = Path.of(conf.path.server_cert_path + "/pi.key");
    val server_csr: Path = Path.of(conf.path.server_cert_path + "/pi.csr");

    val device_ca_cert: Path = Path.of(conf.path.device_ca_cert_path + "/ca.crt");
    val device_cert: Path = Path.of(conf.path.device_cert_path + "/pi.crt");
    val device_key: Path = Path.of(conf.path.device_cert_path + "/pi.key");
    val device_csr: Path = Path.of(conf.path.device_cert_path + "/pi.csr");

    @PostConstruct
    fun initData() {
        /*    인증서 경로타겟을 위해 빈파일을 생성, 하나라도 없으면 안됨.
    - test.ext
    - ca.key
    - ca.crt
    - server.crt
    - server.key
    - server.csr
    - device.crt
    - device.key
    - device.csr*/
        conf.network.ip = cmdLine.searchVirtualIP()
        CoroutineScope(Dispatchers.IO).async {
            var dir = File("${conf.path.server_ca_cert_path}");

            if (dir.mkdirs()) {
                Files.createFile(ca_key);
                Files.createFile(ca_cert);
                dir.setReadable(true);
                dir.setWritable(true);
            }

            dir = File("${conf.path.server_cert_path}");
            if (dir.mkdirs()) {
                Files.createFile(server_cert);
                Files.createFile(server_key);
                Files.createFile(server_csr);
                dir.setReadable(true);
                dir.setWritable(true);
            }

            /*디바이스 인증서*/
            dir = File("${conf.path.device_ca_cert_path}");
            if (dir.mkdirs()) {
                //println("구성함 ${conf.path.device_ca_cert_path}");
                Files.createFile(device_ca_cert);
                dir.setReadable(true);
                dir.setWritable(true);
            }

            dir = File("${conf.path.device_cert_path}");
            if (dir.mkdirs()) {
                Files.createFile(device_cert);
                Files.createFile(device_csr);
                Files.createFile(device_key);
                dir.setReadable(true);
                dir.setWritable(true);
            }

            dir = File("${extPath}");
            if (!dir.exists()) {
                dir.createNewFile();
                dir.setReadable(true);
                dir.setWritable(true);
                dir.printWriter()
                    .use {
                        it.println(
                            "subjectAltName=IP:${cmdLine.searchVirtualIP()}"
                        )
                    }
            }
            var cmd = arrayOf(
                "/bin/bash",
                "-c",
                "echo ${conf.linuxuser.passwd} | sudo -S openssl verify -CAfile ${ca_cert} ${server_cert}"
            );
            var process = Runtime.getRuntime().exec(cmd)
            bufferReadVerify(process, cmd);
        }
    }

    //TODO 서버가 소유한 SSL 인증서 갱신, 검증
    fun refreshServerSSLFile() {
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
    }

    //TODO 인증서파일 Input, Parsing
    fun bufferdFileRead(file: File): Array<String> {
        var results: Array<String> = arrayOf("", "", "");
        if (file.exists()) {
            val stdInput = BufferedReader(InputStreamReader(file.inputStream()));
            var s: String = ""
            while (stdInput.readLine()?.also { s = it } != null) {
                when {
                    s.contains("-----BEGIN") == true -> {
                        results[0] = s + "\n";
                    }
                    s.contains("-----END") == true -> {
                        results[2] = s + "\n";
                    }
                    else -> {
                        results[1] += s + "\n";
                    }
                }
            }
            return results
        } else {
            return results;
        }
    }

    //TODO 디바이스 SSL 정보를 새로 업데이트, 인증서값형식은 변하지 않으므로 고정값으로 문자를 자름
    fun refreshDeviceSSLFile(certificate: Certificate_Save) {
        certificate.cacert = certificate.cacert.replace("\n", "");
        var start: String = certificate.cacert.substring(0, 27)
        var body: String = certificate.cacert.substring(27, certificate.cacert.length - 25)
        var end: String = certificate.cacert.substring(certificate.cacert.length - 25, certificate.cacert.length)
        device_ca_cert.toFile().printWriter().use {
            it.println(start)
            it.println(body)
            it.println(end)
        }

        certificate.devicecert = certificate.devicecert.replace("\n", "");
        start = certificate.devicecert.substring(0, 27)
        body = certificate.devicecert.substring(27, certificate.devicecert.length - 25)
        end = certificate.devicecert.substring(certificate.devicecert.length - 25, certificate.devicecert.length)
        device_cert.toFile().printWriter().use {
            it.println(start)
            it.println(body)
            it.println(end)
        }

        certificate.devicekey = certificate.devicekey.replace("\n", "");
        start = certificate.devicekey.substring(0, 27)
        body = certificate.devicekey.substring(27, certificate.devicekey.length - 25)
        end = certificate.devicekey.substring(certificate.devicekey.length - 25, certificate.devicekey.length)
        device_key.toFile().printWriter().use {
            it.println(start)
            it.println(body)
            it.println(end)
        }

        var cmd: Array<String> =
            arrayOf(
                "/bin/bash",
                "-c",
                "echo ${conf.linuxuser.passwd} | sudo -S openssl verify -CAfile ${device_ca_cert} ${device_cert}"
            );
        var process: Process = Runtime.getRuntime().exec(cmd)
        bufferRead(process, cmd);
    }

    //TODO 인증서 파일 읽기
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

    //TODO 서버 인증서의 유효성 검증
    fun bufferReadVerify(process: Process, command: Array<String>) {
        val stdInput = BufferedReader(InputStreamReader(process.inputStream))
        val stdError = BufferedReader(InputStreamReader(process.errorStream))

        println(command.asList().toString());
        var s: String? = null
        while (stdInput.readLine().also { s = it } != null) {
            if (s.toString().contains("OK")) {
                break;
            }
        }
        while (stdError.readLine().also { s = it } != null) {
            if (s.toString().contains("Error")) {
                //println("서버의 인증서가 유효하지 않아 재발행 합니다.");
                refreshServerSSLFile();
                break;
            }
        }
        process.waitFor();
    }
}
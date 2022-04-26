package com.esg.team.esg.service

import org.springframework.stereotype.Service
import java.security.DigestException
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

//TODO 암호화 및 복호화 작업 입니다. /etc/mosquitto/testPasswd 파일의 비밀번호를 암호화 복호화 하기 위해 사용됩니다
@Service
class CryptionConfigurationIO {
    val SECRET_KEY: String = "B3ncHZeXYQ820zlS68lQAkclY7mIxCIfsJbGYe8xS/I="
    val SECRET_IV: String = "muWSsOb4rk3zE/lXPY9vSQ==";

    fun encrypt(plain_text: String): String {
        //암호화
        val stringToKeySpec: ByteArray = Base64.getDecoder().decode(SECRET_KEY)
        val stringToIvSpec: ByteArray = Base64.getDecoder().decode(SECRET_IV)
        val originalKey: SecretKeySpec = SecretKeySpec(stringToKeySpec, 0, stringToKeySpec.size, "AES")
        val originalIv: IvParameterSpec = IvParameterSpec(stringToIvSpec)
        val cipher_enc = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher_enc.init(Cipher.ENCRYPT_MODE, originalKey, originalIv)
        val byteEncryptedText = cipher_enc.doFinal(plain_text.toByteArray())
        //val keySpecToString: String = Base64.getEncoder().encodeToString(originalKey.encoded);
        //val ivSpecToString: String = Base64.getEncoder().encodeToString(originalIv.iv);
        val encryptedText: String = Base64.getEncoder().encodeToString(byteEncryptedText);
//        println("암호키 : ${keySpecToString}");
//        println("IV 값 : ${ivSpecToString}");
//        println("암호화 : ${encryptedText}");
        return encryptedText
    }

    fun decrypt(secretKey: String, iv: String, cipherText: String) {
        //복호화
//        println("암호키 : ${secretKey}");
//        println("IV 값 : ${iv}");
//        println("암호화 : ${cipherText}");

        val stringToKeySpec: ByteArray = Base64.getDecoder().decode(secretKey)
        val stringToIvSpec: ByteArray = Base64.getDecoder().decode(iv)
        val stringToCipherText: ByteArray = Base64.getDecoder().decode(cipherText)

        val originalKey: SecretKeySpec = SecretKeySpec(stringToKeySpec, 0, stringToKeySpec.size, "AES")
        val originalIv: IvParameterSpec = IvParameterSpec(stringToIvSpec)

        val cipher_dec = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher_dec.init(Cipher.DECRYPT_MODE, originalKey, originalIv)
        val byteDecryptedText = cipher_dec.doFinal(stringToCipherText)
        println("복호화 : " + String(byteDecryptedText));
    }

    fun hashSHA256(msg: String): ByteArray {
        val hash: ByteArray
        try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(msg.toByteArray())
            hash = md.digest()
        } catch (e: CloneNotSupportedException) {
            throw DigestException("couldn't make digest of partial content")
        }
        return hash
    }
}

//fun main() {
//    var configurationIO: CryptionConfigurationIO = CryptionConfigurationIO()
//    //configurationIO.encrypt("Hello World");
//    configurationIO.decrypt(
//        "B3ncHZeXYQ820zlS68lQAkclY7mIxCIfsJbGYe8xS/I=",
//        "muWSsOb4rk3zE/lXPY9vSQ==",
//        "NMLi1nqIxQLhq7BNoZNt7Q=="
//    );
//}
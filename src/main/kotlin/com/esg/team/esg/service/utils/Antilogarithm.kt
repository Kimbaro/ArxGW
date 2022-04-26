package com.esg.team.esg.service.utils

import org.springframework.stereotype.Component

// TODO 2진수, 10진수, subnetmask 변환
@Component
class Antilogarithm {
    //10진수를 서브넷마스크로
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
        println("Antilogarithm.decimalToSubnetmask 작업 완료");
        return decimalToSubnetmask;
    }

    //서브넷마스크를 10진수로
    fun subnetmaskToDecimal(subnet: String): Int {
        var subnetBinary: Int = 0;
        for (data in subnet.split(".")) {
            Integer.toBinaryString(data.toInt()).map { subnetBinary += Character.getNumericValue(it) }
        }
        return subnetBinary;
    }

    fun subnetmaskToDecimal(subnet: String?, nullable: Boolean = true): Int {
        var subnetBinary: Int = 0;
        if (subnet != null) {
            for (data in subnet.split(".")) {
                Integer.toBinaryString(data.toInt()).map { subnetBinary += Character.getNumericValue(it) }
            }
        }
        println("Antilogarithm.subnetmaskToDecimal 작업 완료");
        return subnetBinary;
    }
}
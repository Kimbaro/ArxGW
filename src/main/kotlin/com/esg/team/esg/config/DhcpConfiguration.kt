package com.esg.team.esg.config

import org.springframework.stereotype.Component


/**
 * local server의 netplan의 conf을 write하기 위한 정보
 * */
@Component
class DhcpConfiguration {
    companion object {
        fun r_dhcp_on(name: String): String = "#dhcp=true\n" +
                "package etc.netplan;\n" +
                "hostname\n" +
                "clientid\n" +
                "persistent\n" +
                "option rapid_commit\n" +
                "option domain_name_servers, domain_name, domain_search, host_name\n" +
                "option classless_static_routes\n" +
                "option interface_mtu\n" +
                "require dhcp_server_identifier\n" +
                "slaac private\n" +
                "interface ${name}";

        fun r_dhcp_off(name: String, ip: String?, sm: String?, gw: String?, dns: String?): String = "#dhcp=false\n" +
                "package etc.netplan;\n" +
                "hostname\n" +
                "clientid\n" +
                "persistent\n" +
                "option rapid_commit\n" +
                "option domain_name_servers, domain_name, domain_search, host_name\n" +
                "option classless_static_routes\n" +
                "option interface_mtu\n" +
                "require dhcp_server_identifier\n" +
                "slaac private\n" +
                "interface ${name}\n" +
                "static ip_address=${ip}/${sm}\n" +
                "static routers=${gw}\n" +
                "static domain_name_servers=${dns}\n"
    }
}
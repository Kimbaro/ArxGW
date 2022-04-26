package com.esg.team.esg.config

import org.springframework.stereotype.Component

@Component
class ProxyConfiguration {
    companion object {
        fun keepalived_template(name: String, vip: String, subnet: String, state: String, vid: String): String =
            "vrrp_instance VI_1{\n" +
                    "\n" +
                    "            state ${state}\n" +
                    "\n" +
                    "            interface ${name}\n" +
                    "\n" +
                    "            virtual_router_id ${vid}\n" +
                    "\n" +
                    "            priority 200 advert_int 1\n" +
                    "\n" +
                    "            authentication {\n" +
                    "\n" +
                    "                     auth_type PASS\n" +
                    "\n" +
                    "                     auth_pass 1111\n" +
                    "\n" +
                    "            }\n" +
                    "\n" +
                    "           virtual_ipaddress {${vip}/${subnet}}\n" +
                    "           notify /usr/local/bin/keepalived_notify.sh\n" +
                    "}\n"
    }
}
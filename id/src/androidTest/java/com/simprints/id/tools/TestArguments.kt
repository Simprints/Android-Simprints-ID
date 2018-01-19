package com.simprints.id.tools

import android.os.Bundle
import android.support.test.InstrumentationRegistry


class TestArguments(val scannerMacAddress: String? = null,
                    val wifiNetworkSsid: String? = null,
                    val wifiNetworkPassword: String? = null) {

    companion object {

        private val scannerMacAddressKey = "scanner_mac_address"
        private val wifiNetworkSsidKey = "wifi_network_ssid"
        private val wifiNetworkPasswordKey = "wifi_network_password"

        fun getArguments(): TestArguments {
            val extras: Bundle = InstrumentationRegistry.getArguments()!!

            val result = TestArguments(
                extras[scannerMacAddressKey] as String?,
                extras[wifiNetworkSsidKey] as String?,
                extras[wifiNetworkPasswordKey] as String?
            )

            log("TestArguments.getArguments(): \n$result")

            return result
        }
    }

    override fun toString(): String {
        return """
            $scannerMacAddressKey = $scannerMacAddress
            $wifiNetworkSsidKey = $wifiNetworkSsid
            $wifiNetworkPasswordKey = $wifiNetworkPassword
            """.trimIndent()
    }
}

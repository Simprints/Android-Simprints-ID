package com.simprints.id.tools

import android.os.Bundle
import android.support.test.InstrumentationRegistry


class TestArguments {

    var scannerMacAddress: String? = null
    var wifiNetworkSsid: String? = null
    var wifiNetworkPassword: String? = null

    companion object {

        private val scannerMacAddressKey = "scanner_mac_address"
        private val wifiNetworkSsidKey = "wifi_network_ssid"
        private val wifiNetworkPasswordKey = "wifi_network_password"

        private val none = "none"

        fun getArguments(): TestArguments {
            val extras: Bundle = InstrumentationRegistry.getArguments()!!

            val result = TestArguments()

            if (extras.containsKey(scannerMacAddressKey)) result.scannerMacAddress = extras.getString(scannerMacAddressKey)
            log("TestArguments.getArguments: $scannerMacAddressKey = ${result.scannerMacAddress?: none}")

            if (extras.containsKey(wifiNetworkSsidKey)) result.wifiNetworkSsid = extras.getString(wifiNetworkSsidKey)
            log("TestArguments.getArguments: $wifiNetworkSsidKey = ${result.wifiNetworkSsid?: none}")

            if (extras.containsKey(wifiNetworkPasswordKey)) result.wifiNetworkPassword = extras.getString(wifiNetworkPasswordKey)
            log("TestArguments.getArguments: $wifiNetworkPasswordKey = ${result.wifiNetworkPassword?: none}")

            return result
        }
    }
}

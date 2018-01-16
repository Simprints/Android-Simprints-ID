package com.simprints.id.tools

import android.os.Bundle
import android.support.test.InstrumentationRegistry


object IntentUtils {

    private val scannerMacAddressKey = "scanner_mac_address"
    private val wifiNetworkSsidKey = "wifi_network_ssid"
    private val wifiNetworkPasswordKey = "wifi_network_password"

    private val none = "none"

    class IntentExtras {
        var scannerMacAddress: String? = null
        var wifiNetworkSsid: String? = null
        var wifiNetworkPassword: String? = null
    }

    fun getIntentExtras(): IntentExtras {
        val extras: Bundle = InstrumentationRegistry.getArguments()!!

        val result = IntentExtras()

        if (extras.containsKey(scannerMacAddressKey)) result.scannerMacAddress = extras.getString(scannerMacAddressKey)
        log("IntentUtils.getIntentExtras: $scannerMacAddressKey = ${result.scannerMacAddress?: none}")
        if (extras.containsKey(wifiNetworkSsidKey)) result.wifiNetworkSsid = extras.getString(wifiNetworkSsidKey)
        log("IntentUtils.getIntentExtras: $wifiNetworkSsidKey = ${result.wifiNetworkSsid?: none}")
        if (extras.containsKey(wifiNetworkPasswordKey)) result.wifiNetworkPassword = extras.getString(wifiNetworkPasswordKey)
        log("IntentUtils.getIntentExtras: $wifiNetworkPasswordKey = ${result.wifiNetworkPassword?: none}")
        return result
    }
}

package com.simprints.id.templates

import android.support.test.InstrumentationRegistry
import com.simprints.cerberuslibrary.WifiUtility
import com.simprints.cerberuslibrary.services.UtilityServiceClient
import com.simprints.id.BuildConfig
import com.simprints.id.tools.log
import org.junit.Before


interface HappyWifi {

    @Before
    fun setUp() {
        log("HappyWifi.setUp()")
        val client = UtilityServiceClient(InstrumentationRegistry.getContext())
        val wifiUtility = WifiUtility(client)

        log("HappyWifi.setUp(): ensuring wifi is enabled")
        wifiUtility.setWifiStateSync(true)
        log("HappyWifi.setUp(): ensuring wifi is connected")
        wifiUtility.setWifiNetworkSync(BuildConfig.WIFI_NETWORK, BuildConfig.WIFI_PASSWORD)
        log("HappyWifi.setUp(): ensuring internet is connected")
        wifiUtility.waitForInternetStateSync(true)
    }
}

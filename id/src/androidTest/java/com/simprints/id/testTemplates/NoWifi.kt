package com.simprints.id.testTemplates

import android.support.test.InstrumentationRegistry
import com.simprints.cerberuslibrary.WifiUtility
import com.simprints.cerberuslibrary.services.UtilityServiceClient
import com.simprints.id.testTools.log
import org.junit.Before


interface NoWifi {

    @Before
    fun setUp() {
        log("NoWifi.setUp()")
        val client = UtilityServiceClient(InstrumentationRegistry.getContext())
        val wifiUtility = WifiUtility(client)

        log("NoWifi.setUp(): ensuring wifi is disabled")
        wifiUtility.setWifiStateSync(false)
    }
}

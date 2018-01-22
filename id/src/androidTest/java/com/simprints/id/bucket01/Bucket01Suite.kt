package com.simprints.id.bucket01

import android.support.test.InstrumentationRegistry
import com.simprints.cerberuslibrary.BluetoothUtility
import com.simprints.cerberuslibrary.WifiUtility
import com.simprints.cerberuslibrary.services.UtilityServiceClient
import com.simprints.id.BuildConfig
import com.simprints.id.tools.log
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    HappyWorkflowAllMainFeatures::class,
    HappySyncMediumDatabase::class
)
object Bucket01Suite {

    private val client = UtilityServiceClient(InstrumentationRegistry.getContext())
    private val bluetoothUtility = BluetoothUtility(client)
    private val wifiUtility = WifiUtility(client)

    @BeforeClass
    @JvmStatic
    fun suiteSetUp() {
        log("Bucket01Suite.suiteSetUp()")
        log("Bucket01Suite.suiteSetUp(): ensuring bluetooth is enabled")
        bluetoothUtility.setBluetoothStateSync(true)
        log("Bucket01Suite.suiteSetUp(): ensuring wifi is enabled")
        wifiUtility.setWifiStateSync(true)
        log("Bucket01Suite.suiteSetUp(): ensuring scanner is paired")
        log(BluetoothUtility.convertNameToMacAddress(BuildConfig.SCANNER))
        bluetoothUtility.setBluetoothPairingStateSync(BluetoothUtility.convertNameToMacAddress(BuildConfig.SCANNER), true)
        log("Bucket01Suite.suiteSetUp(): ensuring wifi is connected")
        wifiUtility.setWifiNetworkSync(BuildConfig.WIFI_NETWORK, BuildConfig.WIFI_PASSWORD)
        log("Bucket01Suite.suiteSetUp(): ensuring internet is connected")
        wifiUtility.waitForInternetStateSync(true)
    }

    @AfterClass
    @JvmStatic
    fun suiteTearDown() {
        log("Bucket01Suite.suiteTearDown()")
        log("Bucket01Suite.suiteTearDown(): un-pairing scanner")
        bluetoothUtility.setBluetoothPairingStateSync(BuildConfig.SCANNER, false)
    }
}

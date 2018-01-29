package com.simprints.id.templates

import android.support.test.InstrumentationRegistry
import com.simprints.cerberuslibrary.BluetoothUtility
import com.simprints.cerberuslibrary.services.UtilityServiceClient
import com.simprints.id.BuildConfig
import com.simprints.id.tools.log
import org.junit.After
import org.junit.Before


interface HappyBluetooth {

    @Before
    fun setUp() {
        log("HappyBluetooth.setUp()")
        val client = UtilityServiceClient(InstrumentationRegistry.getContext())
        val bluetoothUtility = BluetoothUtility(client)

        log("HappyBluetooth.setUp(): ensuring bluetooth is enabled")
        bluetoothUtility.setBluetoothStateSync(true)

        val scannerMacAddress = BluetoothUtility.convertNameToMacAddress(BuildConfig.SCANNER)
        log("HappyBluetooth.setUp(): ensuring scanner ${BuildConfig.SCANNER} - $scannerMacAddress is paired")
        bluetoothUtility.setBluetoothPairingStateSync(scannerMacAddress, true)
    }

    @After
    fun tearDown() {
        log("HappyBluetooth.tearDown()")
        val client = UtilityServiceClient(InstrumentationRegistry.getContext())
        val bluetoothUtility = BluetoothUtility(client)

        val scannerMacAddress = BluetoothUtility.convertNameToMacAddress(BuildConfig.SCANNER)
        log("HappyBluetooth.tearDown(): un-pairing scanner ${BuildConfig.SCANNER} - $scannerMacAddress")
        bluetoothUtility.setBluetoothPairingStateSync(scannerMacAddress, true)
    }
}

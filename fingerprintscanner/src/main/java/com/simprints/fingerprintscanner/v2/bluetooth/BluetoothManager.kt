package com.simprints.fingerprintscanner.v2.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.simprints.fingerprintscanner.v2.Scanner
import com.simprints.fingerprintscanner.v2.tools.hexStringToByteArray
import com.simprints.fingerprintscanner.v2.tools.single
import io.reactivex.Single
import java.io.IOException
import java.util.*

class BluetoothManager(private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()) {

    /**
     * @throws BluetoothException
     * @throws IllegalArgumentException
     */
    fun connect(macAddress: String): Single<Scanner> = single {
        bluetoothAdapter ?: throw BluetoothNotSupportedException()

        if (!bluetoothAdapter.isEnabled) throw BluetoothNotEnabledException()

        val device = bluetoothAdapter.getRemoteDevice(
            hexStringToByteArray(
                macAddress
            )
        )

        device.createBond()

        Thread.sleep(8000)

        if (device.bondState != BluetoothDevice.BOND_BONDED) throw BluetoothDeviceNotPairedException(
            macAddress
        )

        try {
            val socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID)
            socket.connect()
            return@single Scanner(socket)
        } catch (e: IOException) {
            throw BluetoothConnectionException(e)
        }
    }

    companion object {
        private val DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SerialPortServiceClass
        private val LAPTOP_UUID = UUID.fromString("00001108-0000-1000-8000-00805F9B34FB") // HeadsetServiceClass
        private val RANDO_UUID = UUID.fromString("00001000-0000-1000-8000-00805F9B34FB")
    }
}

package com.simprints.fingerprintscanner.bluetooth.android

import android.bluetooth.BluetoothDevice
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentDevice
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentSocket
import java.util.*

class AndroidBluetoothDevice(private val device: BluetoothDevice): BluetoothComponentDevice {

    override val name: String = device.name

    override fun isBonded(): Boolean = device.bondState == BluetoothDevice.BOND_BONDED

    override fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothComponentSocket =
            AndroidBluetoothSocket(device.createRfcommSocketToServiceRecord(uuid))

    override val address: String = device.address
}

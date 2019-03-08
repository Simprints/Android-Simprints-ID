package com.simprints.libscanner.bluetooth.android

import android.bluetooth.BluetoothDevice
import com.simprints.libscanner.bluetooth.BluetoothComponentDevice
import com.simprints.libscanner.bluetooth.BluetoothComponentSocket
import java.util.*

class AndroidBluetoothDevice(private val device: BluetoothDevice): BluetoothComponentDevice {

    override val name: String = device.name

    override fun isBonded(): Boolean = device.bondState == BluetoothDevice.BOND_BONDED

    override fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothComponentSocket =
            AndroidBluetoothSocket(device.createRfcommSocketToServiceRecord(uuid))

    override val address: String = device.address
}

package com.simprints.fingerprintscannermock.record

import android.bluetooth.BluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import java.util.*

class AndroidRecordBluetoothDevice(private val device: BluetoothDevice,
                                   private val fileWithFakeBytes: String?): ComponentBluetoothDevice {

    override val name: String = device.name

    override fun isBonded(): Boolean = device.bondState == BluetoothDevice.BOND_BONDED

    override fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket =
            AndroidRecordBluetoothSocket(device.createRfcommSocketToServiceRecord(uuid), fileWithFakeBytes)

    override val address: String = device.address
}

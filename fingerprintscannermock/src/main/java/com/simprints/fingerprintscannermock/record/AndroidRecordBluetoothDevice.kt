package com.simprints.fingerprintscannermock.record

import android.bluetooth.BluetoothDevice
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentDevice
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentSocket
import java.util.*

class AndroidRecordBluetoothDevice(private val device: BluetoothDevice,
                                   private val fileWithFakeBytes: String?): BluetoothComponentDevice {

    override val name: String = device.name

    override fun isBonded(): Boolean = device.bondState == BluetoothDevice.BOND_BONDED

    override fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothComponentSocket =
            AndroidRecordBluetoothSocket(device.createRfcommSocketToServiceRecord(uuid), fileWithFakeBytes)

    override val address: String = device.address
}

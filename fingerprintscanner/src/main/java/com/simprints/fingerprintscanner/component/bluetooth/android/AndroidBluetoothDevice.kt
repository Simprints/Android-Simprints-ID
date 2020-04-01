package com.simprints.fingerprintscanner.component.bluetooth.android

import android.bluetooth.BluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import java.util.*

class AndroidBluetoothDevice(private val device: BluetoothDevice) : ComponentBluetoothDevice {

    override val name: String? = device.name

    override fun isBonded(): Boolean = device.bondState == BluetoothDevice.BOND_BONDED

    override fun createBond(): Boolean = device.createBond()

    override fun removeBond(): Boolean {
        // This method can only be access via reflection
        val method = BluetoothDevice::class.java.getMethod("removeBond")
        return method.invoke(device) as Boolean
    }

    override fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket =
        AndroidBluetoothSocket(device.createRfcommSocketToServiceRecord(uuid))

    override val address: String = device.address
}

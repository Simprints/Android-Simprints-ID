package com.simprints.fingerprint.infra.scanner.component.bluetooth.android

import android.bluetooth.BluetoothDevice
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import java.util.UUID

internal class AndroidBluetoothDevice(
    private val device: BluetoothDevice,
) : ComponentBluetoothDevice {
    override val name: String? = device.name

    override fun isBonded(): Boolean = device.bondState == BluetoothDevice.BOND_BONDED

    override fun createBond(): Boolean = device.createBond()

    override fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket =
        AndroidBluetoothSocket(device.createRfcommSocketToServiceRecord(uuid))

    override val address: String = device.address
}

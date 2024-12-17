package com.simprints.fingerprint.infra.scannermock.record

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import java.util.UUID

class AndroidRecordBluetoothDevice(
    private val device: BluetoothDevice,
    private val context: Context,
) : ComponentBluetoothDevice {
    override val name: String? = device.name

    override fun isBonded(): Boolean = device.bondState == BluetoothDevice.BOND_BONDED

    override fun createBond(): Boolean = device.createBond()

    override fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket = AndroidRecordBluetoothSocket(
        device.createRfcommSocketToServiceRecord(uuid),
        context,
    )

    override val address: String = device.address
}

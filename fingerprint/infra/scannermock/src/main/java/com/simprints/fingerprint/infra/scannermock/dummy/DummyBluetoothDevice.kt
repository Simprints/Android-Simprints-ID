package com.simprints.fingerprint.infra.scannermock.dummy

import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import java.util.UUID
import kotlin.random.Random

class DummyBluetoothDevice(
    override val name: String? = "SP000000",
    override val address: String = "F0:AC:D7:C0:00:00",
    private val isBonded: Boolean = true,
) : ComponentBluetoothDevice {
    override fun isBonded(): Boolean = isBonded

    override fun createBond(): Boolean = true

    override fun createRfcommSocketToServiceRecord(uuid: UUID): ComponentBluetoothSocket =
        throw UnsupportedOperationException("DummyBluetoothDevice::createRfcommSocketToServiceRecord")

    companion object {
        fun random(isBonded: Boolean = true): DummyBluetoothDevice {
            val number = Random.nextInt(1000000)
            val address = "F0:AC:D7:C" + StringBuilder(number.toString(16).padStart(5, '0'))
                .insert(1, ":")
                .insert(4, ":")
                .toString()
            val name = "SP" + number.toString().padStart(6, '0')
            return DummyBluetoothDevice(
                name = name,
                address = address,
                isBonded = isBonded,
            )
        }
    }
}

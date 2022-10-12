package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * This is a helper class used to establish bluetooth socket connection, between the android
 * device and the Vero 2 scanner. It also handles retries and reconnections to the scanner.
 *
 * @param bluetoothAdapter  a reference to the [ComponentBluetoothSocket] for connecting and
 *         disconnecting from the bluetooth socket.
 * @param dispatcher  a [DispatcherProvider] for specifying which context a coroutine should run
 */
class ConnectionHelper @Inject constructor(
    private val bluetoothAdapter: ComponentBluetoothAdapter,
    private val dispatcher: DispatcherProvider
) {

    private var socket: ComponentBluetoothSocket? = null

    /**
     * This method establishes socket connection to the given scanner object, using the provided
     * scanner's mac address and the bluetooth socket [ComponentBluetoothSocket].
     *
     * @param scanner   the scanner object that will be connected to via bluetooth socket
     * @param macAddress  the string value representing the scanner's mac address
     *
     * @return  a flow [Flow] representing the connection process that could occur multiple times
     *                 with retries, hence a flow is primarily used to handle connection retries
     *
     * @throws ScannerDisconnectedException if could not connect with the scanner
     * @throws ScannerNotPairedException if attempted to connect to a scanner that is not paired
     * @throws BluetoothNotEnabledException if bluetooth is not turned on
     * @throws BluetoothNotSupportedException if bluetooth is not supported on this device (e.g. an emulator)
     */
    fun connectScanner(
        scanner: Scanner,
        macAddress: String,
        maxRetries: Long = CONNECT_MAX_RETRIES
    ): Flow<Unit> =
        establishConnectedSocket(macAddress, maxRetries).map { socket ->
            connectScannerObjectWithSocket(scanner, socket)
        }

    private fun establishConnectedSocket(
        macAddress: String,
        maxRetries: Long = CONNECT_MAX_RETRIES
    ): Flow<ComponentBluetoothSocket> =
        getPairedDevice(macAddress)
            .map(::initiateAndReturnSocketConnection)
            .retry(maxRetries)

    private fun getPairedDevice(macAddress: String): Flow<ComponentBluetoothDevice> =
        flow {
            if (bluetoothAdapter.isNull()) throw BluetoothNotSupportedException()
            if (!bluetoothAdapter.isEnabled()) throw BluetoothNotEnabledException()

            val device = bluetoothAdapter.getRemoteDevice(macAddress)

            if (!device.isBonded()) throw ScannerNotPairedException()
            emit(device)
        }

    private suspend fun initiateAndReturnSocketConnection(device: ComponentBluetoothDevice): ComponentBluetoothSocket {
        try {
            Simber.d("Attempting connect...")
            val socket = withContext(dispatcher.io()) {
                device.createRfcommSocketToServiceRecord(DEFAULT_UUID)
            }
            bluetoothAdapter.cancelDiscovery()
            withContext(dispatcher.io()) { socket.connect() }
            this.socket = socket
            return socket
        } catch (e: IOException) {
            throw ScannerDisconnectedException()
        }
    }


    private suspend fun connectScannerObjectWithSocket(
        scanner: Scanner,
        socket: ComponentBluetoothSocket
    ) {
        Simber.d("Socket connected. Setting up scanner...")
        scanner.connect(socket.getInputStream(), socket.getOutputStream()).await()
    }

    suspend fun disconnectScanner(scanner: Scanner) {
        scanner.disconnect().await()
        socket?.close()
    }

    suspend fun reconnect(
        scanner: Scanner,
        macAddress: String,
        maxRetries: Long = CONNECT_MAX_RETRIES
    ) {
        disconnectScanner(scanner)
        delay(RECONNECT_DELAY_MS)
        connectScanner(scanner, macAddress, maxRetries).collect()
    }

    companion object {
        val DEFAULT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        const val CONNECT_MAX_RETRIES = 1L
        const val RECONNECT_DELAY_MS = 1000L
    }
}

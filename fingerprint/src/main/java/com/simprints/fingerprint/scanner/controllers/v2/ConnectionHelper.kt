package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.infra.logging.Simber
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Helper class for connecting to a Vero 2 with retries.
 * Holds a reference to the [ComponentBluetoothSocket] so that it can be disconnected later.
 */
class ConnectionHelper(private val bluetoothAdapter: ComponentBluetoothAdapter,
                       private val timeScheduler: Scheduler = Schedulers.io()) {

    private var socket: ComponentBluetoothSocket? = null

    /**
     * @throws ScannerDisconnectedException if could not connect with the scanner
     * @throws ScannerNotPairedException if attempted to connect to a scanner that is not paired
     * @throws BluetoothNotEnabledException if bluetooth is not turned on
     * @throws BluetoothNotSupportedException if bluetooth is not supported on this device (e.g. an emulator)
     */
    suspend fun connectScanner(scanner: Scanner, macAddress: String, maxRetries: Long = CONNECT_MAX_RETRIES) =
        establishConnectedSocket(macAddress, maxRetries).map { socket ->
            connectScannerObjectWithSocket(scanner, socket)
        }

    private fun establishConnectedSocket(macAddress: String, maxRetries: Long = CONNECT_MAX_RETRIES): Flow<ComponentBluetoothSocket> =
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
            val socket = withContext(Dispatchers.IO) { device.createRfcommSocketToServiceRecord(DEFAULT_UUID) }
            bluetoothAdapter.cancelDiscovery()
            withContext(Dispatchers.IO) { socket.connect() }
            this.socket = socket
            return socket
        } catch (e: IOException) {
            throw ScannerDisconnectedException()
        }
    }


    private suspend fun connectScannerObjectWithSocket(scanner: Scanner, socket: ComponentBluetoothSocket) =
        suspendCoroutine<Unit> {cont ->
            Simber.d("Socket connected. Setting up scanner...")

            // TODO update [Scanner] class to use Coroutines
            scanner.connect(socket.getInputStream(), socket.getOutputStream())
                .doOnError { exception -> cont.resumeWithException(exception) }
                .subscribe { cont.resume(Unit) }
        }

    suspend fun disconnectScanner(scanner: Scanner) =
        suspendCoroutine<Unit> { cont ->
            scanner.disconnect()
                .doOnComplete { socket?.close() }
                .doOnError { error -> cont.resumeWithException(error) }
                .subscribe { cont.resume(Unit) }
        }

    suspend fun reconnect(scanner: Scanner, macAddress: String, maxRetries: Long = CONNECT_MAX_RETRIES) {
        disconnectScanner(scanner)
        delay(RECONNECT_DELAY_MS)
        connectScanner(scanner, macAddress, maxRetries)
    }

    companion object {
        val DEFAULT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        const val CONNECT_MAX_RETRIES = 1L
        const val RECONNECT_DELAY_MS = 1000L
    }
}

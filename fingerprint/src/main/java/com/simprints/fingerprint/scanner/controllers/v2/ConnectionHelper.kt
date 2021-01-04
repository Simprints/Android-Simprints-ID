package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

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
    fun connectScanner(scanner: Scanner, macAddress: String, maxRetries: Long = CONNECT_MAX_RETRIES): Completable =
        establishConnectedSocket(macAddress, maxRetries)
            .flatMapCompletable { socket -> connectScannerObjectWithSocket(scanner, socket) }

    private fun establishConnectedSocket(macAddress: String, maxRetries: Long = CONNECT_MAX_RETRIES): Single<ComponentBluetoothSocket> =
        Single.fromCallable {
            if (bluetoothAdapter.isNull()) throw BluetoothNotSupportedException()
            if (!bluetoothAdapter.isEnabled()) throw BluetoothNotEnabledException()

            val device = bluetoothAdapter.getRemoteDevice(macAddress)

            if (!device.isBonded()) throw ScannerNotPairedException()
            device
        }.flatMap { device ->
            Single.fromCallable {
                try {
                    Timber.d("Attempting connect...")
                    val socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID)
                    bluetoothAdapter.cancelDiscovery()
                    socket.connect()
                    this.socket = socket
                    socket
                } catch (e: IOException) {
                    throw ScannerDisconnectedException()
                }
            }.retry(maxRetries)
        }

    private fun connectScannerObjectWithSocket(scanner: Scanner, socket: ComponentBluetoothSocket): Completable {
        Timber.d("Socket connected. Setting up scanner...")
        return scanner.connect(socket.getInputStream(), socket.getOutputStream())
    }

    fun disconnectScanner(scanner: Scanner): Completable =
        scanner.disconnect()
            .doOnComplete { socket?.close() }

    fun reconnect(scanner: Scanner, macAddress: String, maxRetries: Long = CONNECT_MAX_RETRIES): Completable =
        disconnectScanner(scanner)
            .delay(RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS, timeScheduler)
            .andThen(connectScanner(scanner, macAddress, maxRetries))

    companion object {
        val DEFAULT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        const val CONNECT_MAX_RETRIES = 1L
        const val RECONNECT_DELAY_MS = 1000L
    }
}

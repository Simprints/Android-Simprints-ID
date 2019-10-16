package com.simprints.fingerprintscannermock.simulated

import com.simprints.fingerprintscannermock.simulated.common.ScannerState
import com.simprints.fingerprintscannermock.simulated.common.SimulatedFinger
import com.simprints.fingerprintscannermock.simulated.common.SimulatedScanner
import com.simprints.fingerprintscannermock.simulated.component.SimulatedBluetoothDevice
import com.simprints.fingerprintscannermock.simulated.tools.OutputStreamInterceptor
import com.simprints.fingerprintscannermock.simulated.v1.SimulatedScannerV1
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.atomic.AtomicInteger

class SimulatedScannerManager(
    simulationMode: SimulationMode,
    scannerState: ScannerState = ScannerState(),
    private val simulatedFingers: Array<SimulatedFinger> = SimulatedFinger.person1TwoFingersGoodScan,
    private val pairedScannerAddresses: Set<String> = setOf(DEFAULT_MAC_ADDRESS),
    var isAdapterNull: Boolean = false,
    var isAdapterEnabled: Boolean = true,
    var isDeviceBonded: Boolean = true,
    var deviceName: String = "",
    var outgoingStreamObservers: Set<Observer<ByteArray>> = setOf()) {

    private val simulatedScanner: SimulatedScanner =
        when (simulationMode) {
            SimulationMode.V1 -> SimulatedScannerV1(this, scannerState)
            SimulationMode.V2 -> TODO()
        }

    private val mockFingerIndex = AtomicInteger(0)
    fun currentMockFinger() = simulatedFingers[mockFingerIndex.get()]
    fun cycleToNextFinger() = mockFingerIndex.set((mockFingerIndex.get() + 1) % simulatedFingers.size)

    private lateinit var fakeScannerStream: PipedOutputStream
    lateinit var streamFromScannerToApp: PipedInputStream
    lateinit var streamFromAppToScanner: OutputStreamInterceptor

    var pairedScanners: Set<SimulatedBluetoothDevice>

    init {
        pairedScanners = createScannersFromAddresses()
        refreshStreams()
    }

    private fun createScannersFromAddresses(): Set<SimulatedBluetoothDevice> =
        pairedScannerAddresses
            .map { SimulatedBluetoothDevice(this, it) }
            .toSet()

    fun getScannerWithAddress(address: String): SimulatedBluetoothDevice =
        pairedScanners.firstOrNull { address == it.address }
            ?: SimulatedBluetoothDevice(this, address)

    private fun refreshStreams() {
        this.fakeScannerStream = PipedOutputStream()
        this.streamFromScannerToApp = PipedInputStream().also {
            fakeScannerStream.connect(it)
        }
        this.streamFromAppToScanner = OutputStreamInterceptor()
    }

    fun connect() {
        refreshStreams()
        streamFromAppToScanner.observers.add(appToScannerObserver)
        outgoingStreamObservers.forEach { streamFromAppToScanner.observers.add(it) }
    }

    private val appToScannerObserver = object : DisposableObserver<ByteArray>() {
        override fun onComplete() {}
        override fun onNext(bytes: ByteArray) {
            handleAppToScannerEvent(bytes)
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun handleAppToScannerEvent(bytes: ByteArray) {
        simulatedScanner.handleAppToScannerEvent(bytes, fakeScannerStream)
    }

    fun close() {
        fakeScannerStream.close()
        streamFromScannerToApp.close()
        streamFromAppToScanner.close()
    }

    companion object {
        const val DEFAULT_MAC_ADDRESS: String = "F0:AC:D7:CE:E3:B5"
    }
}

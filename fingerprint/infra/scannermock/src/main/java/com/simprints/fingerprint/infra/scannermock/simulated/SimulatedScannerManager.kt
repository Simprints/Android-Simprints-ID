package com.simprints.fingerprint.infra.scannermock.simulated

import android.content.Context
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulatedFinger
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulatedScanner
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulatedScannerState
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulationSpeedBehaviour
import com.simprints.fingerprint.infra.scannermock.simulated.component.SimulatedBluetoothDevice
import com.simprints.fingerprint.infra.scannermock.simulated.tools.OutputStreamInterceptor
import com.simprints.fingerprint.infra.scannermock.simulated.v1.SimulatedScannerStateV1
import com.simprints.fingerprint.infra.scannermock.simulated.v1.SimulatedScannerV1
import com.simprints.fingerprint.infra.scannermock.simulated.v2.SimulatedScannerStateV2
import com.simprints.fingerprint.infra.scannermock.simulated.v2.SimulatedScannerV2
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.atomic.AtomicInteger

class SimulatedScannerManager(
    val simulationMode: SimulationMode,
    val initialScannerState: SimulatedScannerState? = null,
    val simulationSpeedBehaviour: SimulationSpeedBehaviour = SimulationSpeedBehaviour.INSTANT,
    private val simulatedFingers: Array<SimulatedFinger> = SimulatedFinger.person1TwoFingersGoodScan,
    private val pairedScannerAddresses: Set<String> = setOf(DEFAULT_MAC_ADDRESS),
    var isAdapterNull: Boolean = false,
    var isAdapterEnabled: Boolean = true,
    var isDeviceBonded: Boolean = true,
    var deviceName: String = "",
    var outgoingStreamObservers: Set<(message: ByteArray) -> Unit> = setOf(),
    var context: Context?,
) {
    private var simulatedScanner: SimulatedScanner? = null

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

    private fun createScannersFromAddresses(): Set<SimulatedBluetoothDevice> = pairedScannerAddresses
        .map { SimulatedBluetoothDevice(this, it) }
        .toSet()

    fun getScannerWithAddress(address: String): SimulatedBluetoothDevice = pairedScanners.firstOrNull { address == it.address }
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
        simulatedScanner = when (simulationMode) {
            SimulationMode.V1 -> SimulatedScannerV1(
                this,
                initialScannerState as? SimulatedScannerStateV1
                    ?: SimulatedScannerStateV1(),
            )
            SimulationMode.V2 -> SimulatedScannerV2(
                this,
                initialScannerState as? SimulatedScannerStateV2
                    ?: SimulatedScannerStateV2(),
            )
        }
    }

    private val appToScannerObserver: (message: ByteArray) -> Unit = { bytes ->
        handleAppToScannerEvent(bytes)
    }

    private fun handleAppToScannerEvent(bytes: ByteArray) {
        simulatedScanner?.handleAppToScannerEvent(bytes, fakeScannerStream)
    }

    fun close() {
        fakeScannerStream.close()
        streamFromScannerToApp.close()
        streamFromAppToScanner.close()
        simulatedScanner?.disconnect()
    }

    companion object {
        const val DEFAULT_MAC_ADDRESS: String = "F0:AC:D7:C0:03:B5"
    }
}

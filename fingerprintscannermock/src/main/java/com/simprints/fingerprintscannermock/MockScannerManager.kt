package com.simprints.fingerprintscannermock

import com.simprints.fingerprintscanner.Message
import com.simprints.fingerprintscannermock.ByteArrayUtils.bytesToMessage
import io.reactivex.observers.DisposableObserver
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.atomic.AtomicInteger


class MockScannerManager(val mockFingers: Array<MockFinger> = MockFinger.person1TwoFingersGoodScan,
                         private val pairedScannerAddresses: Set<String> = setOf(DEFAULT_MAC_ADDRESS),
                         var isAdapterNull: Boolean = false,
                         var isAdapterEnabled: Boolean = true,
                         var isDeviceBonded: Boolean = true,
                         var deviceName: String = "") {

    private val mockResponseHelper = MockResponseHelper(this)

    val mockFingerIndex = AtomicInteger(0)
    fun currentMockFinger() = mockFingers[mockFingerIndex.get()]
    fun cycleToNextFinger() = mockFingerIndex.set((mockFingerIndex.get() + 1) % mockFingers.size)

    private lateinit var fakeScannerStream: PipedOutputStream
    lateinit var streamFromScannerToApp: PipedInputStream
    lateinit var streamFromAppToScanner: OutputStreamInterceptor

    var pairedScanners: Set<MockBluetoothDevice>

    init {
        pairedScanners = createScannersFromAddresses()
        refreshStreams()
    }

    private fun createScannersFromAddresses(): Set<MockBluetoothDevice> =
            pairedScannerAddresses
                    .map { MockBluetoothDevice(this, it) }
                    .toSet()

    fun getScannerWithAddress(address: String): MockBluetoothDevice =
            pairedScanners.firstOrNull { address == it.address }
                    ?: MockBluetoothDevice(this, address)

    private fun refreshStreams() {
        this.fakeScannerStream = PipedOutputStream()
        this.streamFromScannerToApp = PipedInputStream().also {
            fakeScannerStream.connect(it)
        }
        this.streamFromAppToScanner = OutputStreamInterceptor()
    }

    fun connect() {
        refreshStreams()
        OutputStreamInterceptor.observers.add(appToScannerObserver)
    }

    private val appToScannerObserver = object : DisposableObserver<ByteArray>() {
        override fun onComplete() {}
        override fun onNext(bytes: ByteArray) { handleAppToScannerEvent(bytes) }
        override fun onError(e: Throwable) { e.printStackTrace() }
    }

    private fun handleAppToScannerEvent(bytes: ByteArray) {
        val message: Message = bytesToMessage(bytes)
        val response: ByteArray = mockResponseHelper.createMockResponse(message, currentMockFinger())
        writeMockedResponseToStream(response)
    }

    private fun writeMockedResponseToStream(response: ByteArray) {
        fakeScannerStream.write(response)
        fakeScannerStream.flush()
    }

    fun close() {
        fakeScannerStream.close()
        streamFromScannerToApp.close()
        streamFromAppToScanner.close()
        OutputStreamInterceptor.observers.remove(appToScannerObserver)
    }

    companion object {
        const val DEFAULT_MAC_ADDRESS: String = "F0:AC:D7:CE:E3:B5"
    }
}

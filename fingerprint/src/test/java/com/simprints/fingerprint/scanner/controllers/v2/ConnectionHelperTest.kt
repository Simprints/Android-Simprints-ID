package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.mockk.*
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.io.IOException

class ConnectionHelperTest {

    private val mockSocket = mockk<ComponentBluetoothSocket> {
        every { getInputStream() } returns mockk()
        every { getOutputStream() } returns mockk()
    }
    private val mockDevice = mockk<ComponentBluetoothDevice> {
        every { createRfcommSocketToServiceRecord(any()) } returns mockSocket
    }
    private val mockAdapter = mockk<ComponentBluetoothAdapter> {
        every { getRemoteDevice(any()) } returns mockDevice
        every { cancelDiscovery() } returns true
    }

    private val mockScanner = mockk<Scanner> {
        every { connect(any(), any()) } returns Completable.complete()
        every { disconnect() } returns Completable.complete()
    }

    private val testScheduler = TestScheduler()
    private val connectionHelper = ConnectionHelper(mockAdapter, testScheduler)

    @Test
    fun connect_successful_connectsScannerAndSocket() = runBlockingTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address")

        verify { mockSocket.connect() }
        verify { mockScanner.connect(any(), any()) }
    }

    @Test(expected = BluetoothNotSupportedException::class)
    fun connect_adapterIsNull_throwsBluetoothNotSupportedException() = runBlockingTest {
        every { mockAdapter.isNull() } returns true

        connectionHelper.connectScanner(mockScanner, "mac address")
    }

    @Test(expected = BluetoothNotEnabledException::class)
    fun connect_adapterIsOff_throwsBluetoothNotEnabledException() = runBlockingTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns false

         connectionHelper.connectScanner(mockScanner, "mac address")
    }

    @Test(expected = ScannerNotPairedException::class)
    fun connect_deviceNotPaired_throwsScannerNotPairedException() = runBlockingTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns false

        connectionHelper.connectScanner(mockScanner, "mac address")
    }

    @Test(expected = ScannerDisconnectedException::class)
    fun connect_socketFailsToConnect_throwsScannerDisconnectedException() = runBlockingTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } throws IOException("Oops")

        connectionHelper.connectScanner(mockScanner, "mac address")
    }

    @Test
    fun connect_socketFailsFirstTimeThenConnects_completesSuccessfullyDueToRetry() = runBlockingTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } throws IOException("Oops") andThen Unit

        connectionHelper.connectScanner(mockScanner, "mac address")

        verify(exactly = 2) { mockSocket.connect() }
        coVerify { mockScanner.connect(any(), any()) }
    }

    @Test
    fun disconnect_disconnectsScanner() = runBlockingTest {
        connectionHelper.disconnectScanner(mockScanner)

        verify { mockScanner.disconnect() }
    }

    @Test
    fun connectThenDisconnect_disconnectsScannerAndSocket() = runBlockingTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit
        every { mockSocket.close() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address")
        connectionHelper.disconnectScanner(mockScanner)


        verify { mockScanner.disconnect() }
        verify { mockSocket.close() }
    }

    @Test
    fun connectThenReconnect_reconnectsSocketAndScanner() = runBlockingTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit
        every { mockSocket.close() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address")
        connectionHelper.reconnect(mockScanner, "mac address")

        verify(Ordering.SEQUENCE) {
            mockScanner.connect(any(), any())
            mockScanner.disconnect()
            mockScanner.connect(any(), any())
        }
        verify(Ordering.ORDERED) {
            mockSocket.connect()
            mockSocket.close()
            mockSocket.connect()
        }
    }
}

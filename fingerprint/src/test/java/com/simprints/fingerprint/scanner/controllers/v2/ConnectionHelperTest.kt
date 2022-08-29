package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import io.mockk.*
import io.reactivex.Completable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ConnectionHelperTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

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

    private val dispatcherProvider = TestDispatcherProvider(testCoroutineRule)


    private val connectionHelper = ConnectionHelper(mockAdapter, dispatcherProvider)

    @Test
    fun connect_successful_connectsScannerAndSocket() = runBlocking {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address").collect()

        verify { mockSocket.connect() }
        verify { mockScanner.connect(any(), any()) }
    }

    @Test(expected = BluetoothNotSupportedException::class)
    fun connect_adapterIsNull_throwsBluetoothNotSupportedException() = runBlocking {
        every { mockAdapter.isNull() } returns true

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
    }

    @Test(expected = BluetoothNotEnabledException::class)
    fun connect_adapterIsOff_throwsBluetoothNotEnabledException() = runBlocking {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns false

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
    }

    @Test(expected = ScannerNotPairedException::class)
    fun connect_deviceNotPaired_throwsScannerNotPairedException() = runBlocking {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns false

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
    }

    @Test(expected = ScannerDisconnectedException::class)
    fun connect_socketFailsToConnect_throwsScannerDisconnectedException() = runBlocking {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } throws IOException("Oops")

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
    }

    @Test
    fun connect_socketFailsFirstTimeThenConnects_completesSuccessfullyDueToRetry() = runBlocking {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } throws IOException("Oops") andThen Unit

        connectionHelper.connectScanner(mockScanner, "mac address").collect()

        verify(exactly = 2) { mockSocket.connect() }
        coVerify { mockScanner.connect(any(), any()) }
    }

    @Test
    fun disconnect_disconnectsScanner() = runBlocking {
        connectionHelper.disconnectScanner(mockScanner)

        verify { mockScanner.disconnect() }
    }

    @Test
    fun connectThenDisconnect_disconnectsScannerAndSocket() = runBlocking {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit
        every { mockSocket.close() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
        connectionHelper.disconnectScanner(mockScanner)


        verify { mockScanner.disconnect() }
        verify { mockSocket.close() }
    }

    @Test
    fun connectThenReconnect_reconnectsSocketAndScanner() = runBlocking {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit
        every { mockSocket.close() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
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

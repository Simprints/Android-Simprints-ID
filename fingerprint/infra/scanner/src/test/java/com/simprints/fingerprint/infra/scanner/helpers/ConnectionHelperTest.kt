package com.simprints.fingerprint.infra.scanner.helpers

import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.Ordering
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
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
        justRun { connect(any(), any()) }
        justRun { disconnect() }
    }

    private val connectionHelper = ConnectionHelper(mockAdapter, testCoroutineRule.testCoroutineDispatcher)

    @Test
    fun connect_successful_connectsScannerAndSocket() = runTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address").collect()

        verify { mockSocket.connect() }
        verify { mockScanner.connect(any(), any()) }
    }

    @Test(expected = BluetoothNotSupportedException::class)
    fun connect_adapterIsNull_throwsBluetoothNotSupportedException() = runTest {
        every { mockAdapter.isNull() } returns true

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
    }

    @Test(expected = BluetoothNotEnabledException::class)
    fun connect_adapterIsOff_throwsBluetoothNotEnabledException() = runTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns false

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
    }

    @Test(expected = ScannerNotPairedException::class)
    fun connect_deviceNotPaired_throwsScannerNotPairedException() = runTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns false

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
    }

    @Test(expected = ScannerDisconnectedException::class)
    fun connect_socketFailsToConnect_throwsScannerDisconnectedException() = runTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } throws IOException("Oops")

        connectionHelper.connectScanner(mockScanner, "mac address").collect()
    }

    @Test
    fun connect_socketFailsFirstTimeThenConnects_completesSuccessfullyDueToRetry() = runTest {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } throws IOException("Oops") andThen Unit

        connectionHelper.connectScanner(mockScanner, "mac address").collect()

        verify(exactly = 2) { mockSocket.connect() }
        coVerify { mockScanner.connect(any(), any()) }
    }

    @Test
    fun disconnect_disconnectsScanner() = runTest {
        connectionHelper.disconnectScanner(mockScanner)

        verify { mockScanner.disconnect() }
    }

    @Test
    fun connectThenDisconnect_disconnectsScannerAndSocket() = runTest {
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
    fun connectThenReconnect_reconnectsSocketAndScanner() = runTest {
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

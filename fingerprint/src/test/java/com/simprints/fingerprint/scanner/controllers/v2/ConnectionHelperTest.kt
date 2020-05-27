package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.reactive.advanceTime
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
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
    fun connect_successful_connectsScannerAndSocket() {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit

        val testSubscriber = connectionHelper.connectScanner(mockScanner, "mac address").test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()

        verify { mockSocket.connect() }
        verify { mockScanner.connect(any(), any()) }
    }

    @Test
    fun connect_adapterIsNull_throwsBluetoothNotSupportedException() {
        every { mockAdapter.isNull() } returns true

        val testSubscriber = connectionHelper.connectScanner(mockScanner, "mac address").test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError(BluetoothNotSupportedException::class.java)
    }

    @Test
    fun connect_adapterIsOff_throwsBluetoothNotEnabledException() {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns false

        val testSubscriber = connectionHelper.connectScanner(mockScanner, "mac address").test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError(BluetoothNotEnabledException::class.java)
    }

    @Test
    fun connect_deviceNotPaired_throwsScannerNotPairedException() {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns false

        val testSubscriber = connectionHelper.connectScanner(mockScanner, "mac address").test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError(ScannerNotPairedException::class.java)
    }

    @Test
    fun connect_socketFailsToConnect_throwsScannerDisconnectedException() {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } throws IOException("Oops")

        val testSubscriber = connectionHelper.connectScanner(mockScanner, "mac address").test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError(ScannerDisconnectedException::class.java)
    }

    @Test
    fun connect_socketFailsFirstTimeThenConnects_completesSuccessfullyDueToRetry() {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } throws IOException("Oops") andThen Unit

        val testSubscriber = connectionHelper.connectScanner(mockScanner, "mac address").test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()

        verify(exactly = 2) { mockSocket.connect() }
        verify { mockScanner.connect(any(), any()) }
    }

    @Test
    fun disconnect_disconnectsScanner() {
        connectionHelper.disconnectScanner(mockScanner)
            .test().also { testScheduler.advanceTime() }.awaitAndAssertSuccess()

        verify { mockScanner.disconnect() }
    }

    @Test
    fun connectThenDisconnect_disconnectsScannerAndSocket() {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit
        every { mockSocket.close() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address")
            .test().also { testScheduler.advanceTime() }.awaitAndAssertSuccess()
        connectionHelper.disconnectScanner(mockScanner)
            .test().also { testScheduler.advanceTime() }.awaitAndAssertSuccess()

        verify { mockScanner.disconnect() }
        verify { mockSocket.close() }
    }

    @Test
    fun connectThenReconnect_reconnectsSocketAndScanner() {
        every { mockAdapter.isNull() } returns false
        every { mockAdapter.isEnabled() } returns true
        every { mockDevice.isBonded() } returns true
        every { mockSocket.connect() } returns Unit
        every { mockSocket.close() } returns Unit

        connectionHelper.connectScanner(mockScanner, "mac address")
            .test().also { testScheduler.advanceTime() }.awaitAndAssertSuccess()
        connectionHelper.reconnect(mockScanner, "mac address")
            .test().also { testScheduler.advanceTime() }.awaitAndAssertSuccess()

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

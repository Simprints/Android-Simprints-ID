package com.simprints.fingerprint.infra.scanner.wrapper

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerInfo
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ScannerFactoryTest {
    private lateinit var scannerFactory: ScannerFactory

    @MockK
    private lateinit var componentBluetoothAdapter: ComponentBluetoothAdapter

    @MockK(relaxed = true)
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var scannerUiHelper: ScannerUiHelper

    @MockK
    private lateinit var serialNumberConverter: SerialNumberConverter

    @MockK
    private lateinit var scannerGenerationDeterminer: ScannerGenerationDeterminer

    @MockK(relaxed = true)
    private lateinit var fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory
    private val scannerInfo = ScannerInfo()

    @MockK
    private lateinit var scannerV2: Scanner

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        scannerFactory = ScannerFactory(
            componentBluetoothAdapter,
            configManager,
            scannerUiHelper,
            serialNumberConverter,
            scannerGenerationDeterminer,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            fingerprintCaptureWrapperFactory,
            mockk(),
            scannerInfo,
            scannerV2,
        )
    }

    @Test
    fun `initScannerOperationWrappers should call creates the correct V1 wrappers`() = runTest {
        // Given
        val macAddress = "F0:AC:D7:C0:01:00"
        val serialNumber = "serialNumber"
        every { serialNumberConverter.convertMacAddressToSerialNumber(macAddress) } returns serialNumber
        every {
            scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber(serialNumber)
        } returns FingerprintConfiguration.VeroGeneration.VERO_1

        // When
        scannerFactory.initScannerOperationWrappers(macAddress)
        // Then
        Truth.assertThat(scannerFactory.scannerV1).isNotNull()
        Truth
            .assertThat(scannerFactory.scannerWrapper)
            .isInstanceOf(ScannerWrapperV1::class.java)
        Truth.assertThat(scannerFactory.scannerOtaOperationsWrapper).isNull()
        verify { fingerprintCaptureWrapperFactory.createV1(any()) }
    }

    @Test
    fun `initScannerOperationWrappers should call creates the correct V2 wrappers`() = runTest {
        // Given
        val macAddress = "F0:AC:D7:C0:01:00"
        val serialNumber = "serialNumber"
        every { serialNumberConverter.convertMacAddressToSerialNumber(macAddress) } returns serialNumber
        every {
            scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber(serialNumber)
        } returns FingerprintConfiguration.VeroGeneration.VERO_2

        // When
        scannerFactory.initScannerOperationWrappers(macAddress)
        // Then
        Truth.assertThat(scannerInfo.scannerId).isEqualTo(serialNumber)
        Truth
            .assertThat(scannerFactory.scannerWrapper)
            .isInstanceOf(ScannerWrapperV2::class.java)
        Truth.assertThat(scannerFactory.scannerOtaOperationsWrapper).isNotNull()
        verify { fingerprintCaptureWrapperFactory.createV2(any()) }
    }
}

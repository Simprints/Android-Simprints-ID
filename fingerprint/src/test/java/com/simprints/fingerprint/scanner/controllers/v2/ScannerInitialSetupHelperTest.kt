package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.AvailableOta
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class ScannerInitialSetupHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val firmwareFileManagerMock = mockk<FirmwareFileManager>()
    private val scannerInitialSetupHelper = ScannerInitialSetupHelper(firmwareFileManagerMock)

    @Before
    fun setup() {
        every { scannerMock.enterMainMode() } returns Completable.complete()
    }

    @Test
    fun ifNoAvailableVersions_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns null

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock) {}.test()

        testSubscriber.awaitAndAssertSuccess()
    }

    @Test
    fun setupScannerWithOtaCheck_savesVersionNumber() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns null

        var version: ScannerVersion? = null

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock) { version = it }.test()

        testSubscriber.awaitAndAssertSuccess()

        assertThat(version).isEqualTo(SCANNER_VERSION_LOW.toScannerVersion())
    }

    @Test
    fun ifAvailableVersionMatchesExistingVersion_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSION_LOW.toScannerVersion().firmware

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock) {}.test()

        testSubscriber.awaitAndAssertSuccess()
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_throwsOtaAvailableException() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSION_HIGH.toScannerVersion().firmware

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock) {}.test()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError { e ->
            e is OtaAvailableException && e.availableOtas.containsAll(listOf(AvailableOta.CYPRESS, AvailableOta.UN20))
        }
    }

    companion object {

        val SCANNER_VERSION_LOW = UnifiedVersionInformation(10L,
            CypressFirmwareVersion(12, 13, 14, 15),
            StmFirmwareVersion(1, 2, 3, 4),
            Un20AppVersion(5, 6, 7, 8))

        val SCANNER_VERSION_HIGH = UnifiedVersionInformation(10L,
            CypressFirmwareVersion(12, 13, 14, 16),
            StmFirmwareVersion(1, 2, 3, 4),
            Un20AppVersion(5, 6, 8, 8))
    }
}

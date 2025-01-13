package com.simprints.fingerprint.infra.scanner.v2.scanner

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.channel.MainMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.RootMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedHardwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.UnifiedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetHardwareVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.SetVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ScannerExtendedInfoReaderHelperTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    val mockRootMessageInputStream = mockk<RootMessageInputStream> {
        justRun { connect(any()) }
        justRun { disconnect() }
    }
    private lateinit var expectedVersionResponse: GetVersionResponse
    private lateinit var expectedHardwareResponse: GetHardwareVersionResponse
    private lateinit var expectedExtendedVersionResponse: GetExtendedVersionResponse
    private lateinit var expectedCypressVersionResponse: GetCypressVersionResponse
    private lateinit var expectedCypressExtendedVersionResponse: GetCypressExtendedVersionResponse
    private lateinit var expectedSetVersionResponse: SetVersionResponse
    private val mainMessageChannel: MainMessageChannel = mockk()
    private val rootMessageChannel: RootMessageChannel = getRootMessageChannel()
    private val scannerInfoReader = ScannerExtendedInfoReaderHelper(
        mainMessageChannel,
        rootMessageChannel,
    )

    @Test
    fun shouldReturn_scannerInformation_withLegacyFirmwareInfo_whenCypressVersion_isOldApi() = runTest {
        val stmMajorFirmwareVersion: Short = 1
        val stmMinorFirmwareVersion: Short = 2
        val un20MajorFirmwareVersion: Short = 1
        val un20MinorFirmwareVersion: Short = 1

        val expectedCypressVersion = CypressFirmwareVersion(1, 0, 1, 0)
        expectedCypressVersionResponse = GetCypressVersionResponse(expectedCypressVersion)

        val expectedUnifiedVersion = UnifiedVersionInformation(
            masterFirmwareVersion = 1202022L,
            cypressFirmwareVersion = expectedCypressVersion,
            stmFirmwareVersion = StmFirmwareVersion(
                1,
                1,
                stmMajorFirmwareVersion,
                stmMinorFirmwareVersion,
            ),
            un20AppVersion = Un20AppVersion(
                1,
                1,
                un20MajorFirmwareVersion,
                un20MinorFirmwareVersion,
            ),
        )
        expectedVersionResponse = GetVersionResponse(expectedUnifiedVersion)
        expectedVersionResponse
        val expectedHardware = "E-1"
        val expectedFirmwareVersions = ExtendedVersionInformation(
            cypressFirmwareVersion = CypressExtendedFirmwareVersion(
                "${expectedCypressVersion.firmwareMajorVersion}.$expectedHardware.${expectedCypressVersion.firmwareMinorVersion}",
            ),
            stmFirmwareVersion = StmExtendedFirmwareVersion(
                "$stmMajorFirmwareVersion.$expectedHardware.$stmMinorFirmwareVersion",
            ),
            un20AppVersion = Un20ExtendedAppVersion(
                "$un20MajorFirmwareVersion.$expectedHardware.$un20MinorFirmwareVersion",
            ),
        )

        every { mockRootMessageInputStream.rootResponseStream } returns flowOf(
            expectedCypressVersionResponse,
            expectedVersionResponse,
        )
        val scannerInformation = scannerInfoReader.readScannerInfo()
        assertThat(scannerInformation.hardwareVersion).isEqualTo("E-1")
        assertThat(scannerInformation.firmwareVersions).isEqualTo(expectedFirmwareVersions)
    }

    @Test
    fun shouldReturn_scannerInformation_withExtendedFirmwareInfo_whenCypressVersion_isNewApi() = runTest {
        val expectedCypressVersion = CypressFirmwareVersion(1, 3, 1, 3)
        expectedCypressVersionResponse = GetCypressVersionResponse(expectedCypressVersion)

        val expectedHardware = "F-1"
        expectedHardwareResponse = GetHardwareVersionResponse(
            ExtendedHardwareVersion(expectedHardware),
        )

        val expectedFirmwareVersions = ExtendedVersionInformation(
            cypressFirmwareVersion = CypressExtendedFirmwareVersion("1.$expectedHardware.3"),
            stmFirmwareVersion = StmExtendedFirmwareVersion("1.$expectedHardware.2"),
            un20AppVersion = Un20ExtendedAppVersion("1.$expectedHardware.1"),
        )
        expectedExtendedVersionResponse = GetExtendedVersionResponse(expectedFirmwareVersions)
        every { mockRootMessageInputStream.rootResponseStream } returns flowOf(
            expectedCypressVersionResponse,
            expectedHardwareResponse,
            expectedExtendedVersionResponse,
        )
        val scannerInformation = scannerInfoReader.readScannerInfo()
        assertThat(scannerInformation.hardwareVersion).isEqualTo(expectedHardware)
        assertThat(scannerInformation.firmwareVersions).isEqualTo(expectedFirmwareVersions)
    }

    @Test
    fun shouldReturn_scannerInformation_containingOld_firmwareVersion_wheneverPartial_otaUpdateOccurs() = runTest {
        val expectedCypressVersion = CypressFirmwareVersion(1, 3, 1, 3)
        expectedCypressVersionResponse = GetCypressVersionResponse(expectedCypressVersion)

        val expectedHardware = "F-1"
        expectedHardwareResponse = GetHardwareVersionResponse(
            ExtendedHardwareVersion(expectedHardware),
        )

        val firmwareVersions = ExtendedVersionInformation(
            cypressFirmwareVersion = CypressExtendedFirmwareVersion("1.$expectedHardware.3"),
            stmFirmwareVersion = StmExtendedFirmwareVersion(""),
            un20AppVersion = Un20ExtendedAppVersion(""),
        )
        expectedExtendedVersionResponse = GetExtendedVersionResponse(firmwareVersions)

        val expectedUnifiedVersion = UnifiedVersionInformation(
            masterFirmwareVersion = 1202022L,
            cypressFirmwareVersion = expectedCypressVersion,
            stmFirmwareVersion = StmFirmwareVersion(1, 1, 1, 2),
            un20AppVersion = Un20AppVersion(1, 1, 1, 1),
        )
        expectedVersionResponse = GetVersionResponse(expectedUnifiedVersion)

        every { mockRootMessageInputStream.rootResponseStream } returns flowOf(
            expectedCypressVersionResponse,
            expectedHardwareResponse,
            expectedExtendedVersionResponse,
            expectedVersionResponse,
        )
        // merge extended version-info, with old-api's unified version-info
        // for missing version values.
        val expectedFirmwareVersions = firmwareVersions.copy(
            stmFirmwareVersion = StmExtendedFirmwareVersion(
                expectedUnifiedVersion.stmFirmwareVersion.toNewVersionNamingScheme(),
            ),
            un20AppVersion = Un20ExtendedAppVersion(
                expectedUnifiedVersion.un20AppVersion.toNewVersionNamingScheme(),
            ),
        )

        val scannerInformation = scannerInfoReader.readScannerInfo()

        assertThat(scannerInformation.hardwareVersion).isEqualTo(expectedHardware)
        assertThat(scannerInformation.firmwareVersions).isEqualTo(expectedFirmwareVersions)
    }

    @Test
    fun shouldReturn_scannerInformation_containingCypressExtendedVersion() = runTest {
        val expectedCypressVersion = CypressExtendedFirmwareVersion("3.E-1.4")
        expectedCypressExtendedVersionResponse =
            GetCypressExtendedVersionResponse(expectedCypressVersion)
        every { mockRootMessageInputStream.rootResponseStream } returns flowOf(
            expectedCypressExtendedVersionResponse,
        )
        val scannerInformation = scannerInfoReader.getCypressExtendedVersion()

        assertThat(scannerInformation.versionAsString).isEqualTo(expectedCypressVersion.versionAsString)
    }

    @Test
    fun shouldReturn_scannerInformation_containingsetExtendedVersionInformationResponse() = runTest {
        expectedSetVersionResponse = SetVersionResponse()
        every { mockRootMessageInputStream.rootResponseStream } returns flowOf(
            expectedSetVersionResponse,
        )
        val scannerInformation = scannerInfoReader.setExtendedVersionInformation(mockk())

        assertThat(scannerInformation).isEqualTo(expectedSetVersionResponse)
    }

    private fun getRootMessageChannel(): RootMessageChannel {
        val mockRootMessageOutputStream = mockk<RootMessageOutputStream> {
            coJustRun { sendMessage(any()) }
        }

        return RootMessageChannel(
            mockRootMessageInputStream,
            mockRootMessageOutputStream,
            Dispatchers.IO,
        )
    }
}

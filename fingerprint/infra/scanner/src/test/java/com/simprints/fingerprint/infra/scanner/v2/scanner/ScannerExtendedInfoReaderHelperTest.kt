package com.simprints.fingerprint.infra.scanner.v2.scanner

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.channel.MainMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.RootMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetCypressExtendedVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetCypressVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetExtendedVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetHardwareVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedHardwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ScannerInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.UnifiedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetHardwareVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.setupMock
import com.simprints.testtools.common.syntax.spy
import com.simprints.testtools.common.syntax.whenThis
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.junit.Rule
import org.junit.Test

class ScannerExtendedInfoReaderHelperTest {


    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val mainMessageChannel: MainMessageChannel = mock()
    private val rootMessageChannel: RootMessageChannel = getRootMessageChannel()
    private val responseErrorHandler: ResponseErrorHandler =  ResponseErrorHandler(
        ResponseErrorHandlingStrategy.NONE)

    private lateinit var expectedVersionResponse: GetVersionResponse
    private lateinit var expectedHardwareResponse: GetHardwareVersionResponse
    private lateinit var expectedExtendedVersionResponse: GetExtendedVersionResponse
    private lateinit var expectedCypressVersionResponse: GetCypressVersionResponse
    private lateinit var expectedCypressExtendedVersionResponse: GetCypressExtendedVersionResponse

    private val scannerInfoReader = ScannerExtendedInfoReaderHelper(
        mainMessageChannel,
        rootMessageChannel,
        responseErrorHandler
    )


    @Test
    fun shouldReturn_scannerInformation_withLegacyFirmwareInfo_whenCypressVersion_isOldApi() {
        val stmMajorFirmwareVersion: Short = 1
        val stmMinorFirmwareVersion: Short = 2
        val un20MajorFirmwareVersion: Short = 1
        val un20MinorFirmwareVersion: Short = 1


        val expectedCypressVersion = CypressFirmwareVersion(1, 0, 1, 0)
        expectedCypressVersionResponse = GetCypressVersionResponse(expectedCypressVersion)

        val expectedUnifiedVersion = UnifiedVersionInformation(
            masterFirmwareVersion = 1202022L,
            cypressFirmwareVersion = expectedCypressVersion,
            stmFirmwareVersion = StmFirmwareVersion(1, 1,
                stmMajorFirmwareVersion, stmMinorFirmwareVersion),
            un20AppVersion = Un20AppVersion(1, 1,
                un20MajorFirmwareVersion, un20MinorFirmwareVersion)
        )
        expectedVersionResponse = GetVersionResponse(expectedUnifiedVersion)

        val expectedHardware = "E-1"
        val expectedFirmwareVersions = ExtendedVersionInformation(
            cypressFirmwareVersion = CypressExtendedFirmwareVersion(
                "${expectedCypressVersion.firmwareMajorVersion}.$expectedHardware.${expectedCypressVersion.firmwareMinorVersion}"),
            stmFirmwareVersion = StmExtendedFirmwareVersion(
                "$stmMajorFirmwareVersion.$expectedHardware.$stmMinorFirmwareVersion"),
            un20AppVersion = Un20ExtendedAppVersion(
                "$un20MajorFirmwareVersion.$expectedHardware.$un20MinorFirmwareVersion")
        )

        val testObserver = scannerInfoReader.readScannerInfo().test()
        testObserver.awaitAndAssertSuccess()


        testObserver.assertValueCount(1)
        val scannerInformation = testObserver.values().first() as ScannerInformation
        assertThat(scannerInformation.hardwareVersion).isEqualTo("E-1")
        assertThat(scannerInformation.firmwareVersions).isEqualTo(
            expectedFirmwareVersions
        )
    }


    @Test
    fun shouldReturn_scannerInformation_withExtendedFirmwareInfo_whenCypressVersion_isNewApi() {
        val expectedCypressVersion = CypressFirmwareVersion(1, 3, 1, 3)
        expectedCypressVersionResponse = GetCypressVersionResponse(expectedCypressVersion)

        val expectedHardware = "F-1"
        expectedHardwareResponse = GetHardwareVersionResponse(
            ExtendedHardwareVersion(expectedHardware)
        )

        val expectedFirmwareVersions = ExtendedVersionInformation(
            cypressFirmwareVersion = CypressExtendedFirmwareVersion("1.$expectedHardware.3"),
            stmFirmwareVersion = StmExtendedFirmwareVersion("1.$expectedHardware.2"),
            un20AppVersion = Un20ExtendedAppVersion("1.$expectedHardware.1")
        )
        expectedExtendedVersionResponse = GetExtendedVersionResponse(expectedFirmwareVersions)



        val testObserver = scannerInfoReader.readScannerInfo().test()
        testObserver.awaitAndAssertSuccess()


        testObserver.assertValueCount(1)
        val scannerInformation = testObserver.values().first() as ScannerInformation
        assertThat(scannerInformation.hardwareVersion).isEqualTo(expectedHardware)
        assertThat(scannerInformation.firmwareVersions).isEqualTo(expectedFirmwareVersions)
    }

    @Test
    fun shouldReturn_scannerInformation_containingOld_firmwareVersion_wheneverPartial_otaUpdateOccurs() {
        val expectedCypressVersion = CypressFirmwareVersion(1, 3, 1, 3)
        expectedCypressVersionResponse = GetCypressVersionResponse(expectedCypressVersion)

        val expectedHardware = "F-1"
        expectedHardwareResponse = GetHardwareVersionResponse(
            ExtendedHardwareVersion(expectedHardware)
        )

        val firmwareVersions = ExtendedVersionInformation(
            cypressFirmwareVersion = CypressExtendedFirmwareVersion("1.$expectedHardware.3"),
            stmFirmwareVersion = StmExtendedFirmwareVersion(""),
            un20AppVersion = Un20ExtendedAppVersion("")
        )
        expectedExtendedVersionResponse = GetExtendedVersionResponse(firmwareVersions)


        val expectedUnifiedVersion = UnifiedVersionInformation(
            masterFirmwareVersion = 1202022L,
            cypressFirmwareVersion = expectedCypressVersion,
            stmFirmwareVersion = StmFirmwareVersion(1, 1, 1, 2),
            un20AppVersion = Un20AppVersion(1, 1, 1, 1)
        )
        expectedVersionResponse = GetVersionResponse(expectedUnifiedVersion)


        // merge extended version-info, with old-api's unified version-info
        // for missing version values.
        val expectedFirmwareVersions = firmwareVersions.copy(
            stmFirmwareVersion = StmExtendedFirmwareVersion(
                expectedUnifiedVersion.stmFirmwareVersion.toNewVersionNamingScheme()),

            un20AppVersion = Un20ExtendedAppVersion(
                expectedUnifiedVersion.un20AppVersion.toNewVersionNamingScheme())
        )


        val testObserver = scannerInfoReader.readScannerInfo().test()
        testObserver.awaitAndAssertSuccess()

        testObserver.assertValueCount(1)
        val scannerInformation = testObserver.values().first() as ScannerInformation
        assertThat(scannerInformation.hardwareVersion).isEqualTo(expectedHardware)
        assertThat(scannerInformation.firmwareVersions).isEqualTo(expectedFirmwareVersions)
    }


    private fun getRootMessageChannel(): RootMessageChannel {
        val responseSubject = PublishSubject.create<RootResponse>()

        val spyRootMessageInputStream = spy(RootMessageInputStream(mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            whenThis { disconnect() } thenDoNothing {}
            rootResponseStream = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        val mockRootMessageOutputStream = setupMock<RootMessageOutputStream> {
            whenThis { sendMessage(anyNotNull()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(
                        when (it.arguments[0] as RootCommand) {
                            is GetVersionCommand -> expectedVersionResponse
                            is GetExtendedVersionCommand -> expectedExtendedVersionResponse
                            is GetCypressVersionCommand -> expectedCypressVersionResponse
                            is GetCypressExtendedVersionCommand -> expectedCypressExtendedVersionResponse
                            is GetHardwareVersionCommand -> expectedHardwareResponse
                            else -> throw IllegalArgumentException()
                        }
                    )
                }
            }
        }

        return RootMessageChannel(spyRootMessageInputStream, mockRootMessageOutputStream)
    }
}

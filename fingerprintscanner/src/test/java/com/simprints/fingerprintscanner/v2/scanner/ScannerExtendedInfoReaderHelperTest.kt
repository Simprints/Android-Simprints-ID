package com.simprints.fingerprintscanner.v2.scanner

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.channel.MainMessageChannel
import com.simprints.fingerprintscanner.v2.channel.RootMessageChannel
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.domain.root.commands.*
import com.simprints.fingerprintscanner.v2.domain.root.models.*
import com.simprints.fingerprintscanner.v2.domain.root.responses.*
import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.rxSingle
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
        val expectedCypressVersion = CypressFirmwareVersion(1, 0, 1, 0)
        expectedCypressVersionResponse = GetCypressVersionResponse(expectedCypressVersion)

        val expectedUnifiedVersion = UnifiedVersionInformation(
            masterFirmwareVersion = 1202022L,
            cypressFirmwareVersion = expectedCypressVersion,
            stmFirmwareVersion = StmFirmwareVersion(1, 1, 1, 2),
            un20AppVersion = Un20AppVersion(1, 1, 1, 1)
        )
        expectedVersionResponse = GetVersionResponse(expectedUnifiedVersion)


        val testObserver = scannerInfoReader.readScannerInfo().test()
        testObserver.awaitAndAssertSuccess()


        testObserver.assertValueCount(1)
        val scannerInformation = testObserver.values().first() as ScannerInformation
        assertThat(scannerInformation.hardwareVersion).isEqualTo("E-1")
        assertThat(scannerInformation.firmwareVersions).isEqualTo(
            expectedUnifiedVersion.toExtendedVersionInfo()
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

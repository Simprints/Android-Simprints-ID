package com.simprints.fingerprint.infra.scanner.v2.scanner

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.MainMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.RootMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageFormat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.TemplateData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageQualityPreviewResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageQualityResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetTemplateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.BatteryCurrent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.BatteryPercentCharge
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.BatteryTemperature
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.BatteryVoltage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.OperationResultCode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.SmileLedState
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryCurrentResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryPercentChargeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryTemperatureResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryVoltageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ScannerInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterCypressOtaModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterStmOtaModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.IllegalUn20StateException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.IncorrectModeException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.main.MainMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.scanner.ota.cypress.CypressOtaController
import com.simprints.fingerprint.infra.scanner.v2.scanner.ota.stm.StmOtaController
import com.simprints.fingerprint.infra.scanner.v2.scanner.ota.un20.Un20OtaController
import com.simprints.fingerprint.infra.scanner.v2.tools.asFlow
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.CoroutineContext

class ScannerTest {
    private lateinit var mockkMessageOutputStream: MainMessageOutputStream
    private lateinit var mockkMessageInputStream: MainMessageInputStream
    private lateinit var scanner: Scanner

    private lateinit var mockkInputStream: InputStream
    private val un20OtaController: Un20OtaController = mockk()
    private val mockkOutputStream = mockk<OutputStream>()
    private val scannerInfo = ScannerInfo()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val coroutineContext: CoroutineContext = Dispatchers.Unconfined
    private val dispatcher = Dispatchers.Unconfined

    @Before
    fun setup() {
        mockkMessageInputStream = setupMainMessageInputStreamMock()
        mockkMessageOutputStream = mockk {
            justRun { connect(any()) }
            justRun { disconnect() }
            justRun { sendMessage(any()) }
        }

        mockkStatic("com.simprints.fingerprint.infra.scanner.v2.tools.InputStreamToFlowKt")
        mockkInputStream = mockk(relaxed = true) {
            every { asFlow(dispatcher) } returns emptyFlow<ByteArray>()
        }
        scanner = Scanner(
            MainMessageChannel(mockkMessageInputStream, mockkMessageOutputStream, coroutineContext),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            un20OtaController,
            scannerInfo,
            dispatcher,
        )
    }

    @Test(expected = NotConnectedException::class)
    fun scanner_callEnterModeBeforeConnect_throwsException() = runTest {
        scanner.enterMainMode()
    }

    @Test
    fun scanner_call_batteryInfo() = runTest {
        val expectedPercentCharge = 1
        val expectedVoltageMilliVolts = 2
        val expectedCurrentMilliAmps = 3
        val expectedTemperatureDeciKelvin = 4

        val messageInputStreamMock = setupMainMessageInputStreamMock(
            veroMessages = listOf<VeroResponse>(
                GetBatteryPercentChargeResponse(BatteryPercentCharge(expectedPercentCharge.toByte())),
                GetBatteryTemperatureResponse(BatteryTemperature(expectedTemperatureDeciKelvin.toShort())),
                GetBatteryVoltageResponse(BatteryVoltage(expectedVoltageMilliVolts.toShort())),
                GetBatteryCurrentResponse(BatteryCurrent(expectedCurrentMilliAmps.toShort())),
            ),
        )

        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        assertThat(scanner.getBatteryPercentCharge()).isEqualTo(expectedPercentCharge)
        assertThat(scanner.getBatteryVoltageMilliVolts()).isEqualTo(expectedVoltageMilliVolts)
        assertThat(scanner.getBatteryCurrentMilliAmps()).isEqualTo(expectedCurrentMilliAmps)
        assertThat(scanner.getBatteryTemperatureDeciKelvin()).isEqualTo(
            expectedTemperatureDeciKelvin,
        )
    }

    @Test
    fun scanner_call_getUn20Status() = runTest {
        val expectedValue = true

        val messageInputStreamMock =
            setupMainMessageInputStreamMock(veroMessages = listOf(GetUn20OnResponse(DigitalValue.TRUE)))

        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)

        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()

        val result = scanner.getUn20Status()
        assertThat(result).isEqualTo(expectedValue)
    }

    @Test
    fun scanner_call_getImageQualityScore() = runTest {
        val expectedValue = GetImageQualityResponse(1)

        val messageInputStreamMock =
            setupMainMessageInputStreamMock(un20Messages = listOf(expectedValue))

        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)

        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.state.un20On = true
        val result = scanner.getImageQualityScore()
        assertThat(result).isEqualTo(expectedValue.imageQualityScore)
    }

    @Test
    fun scanner_call_getUn20AppVersion() = runTest {
        val expectedValue = Un20ExtendedAppVersion("un20Version")
        val scannerInfoReaderMockk = mockk<ScannerExtendedInfoReaderHelper> {
            coEvery { getUn20ExtendedAppVersion() } returns expectedValue
        }
        val scanner = createScanner(scannerInfoReaderMockk)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.state.un20On = true
        val result = scanner.getUn20AppVersion()
        assertThat(result).isEqualTo(expectedValue)
    }

    @Test
    fun scanner_call_getVersionInformation() = runTest {
        val expectedValue = mockk<ScannerInformation>()

        val scannerInfoReaderMockk = mockk<ScannerExtendedInfoReaderHelper> {
            coEvery { readScannerInfo() } returns expectedValue
        }
        val scanner = createScanner(scannerInfoReaderMockk)
        scanner.connect(mockkInputStream, mockkOutputStream)

        val result = scanner.getVersionInformation()
        assertThat(result).isEqualTo(expectedValue)
    }

    @Test
    fun scanner_call_setVersionInformation() = runTest {
        val expectedValue = mockk<ExtendedVersionInformation>()

        val scannerInfoReaderMockk = mockk<ScannerExtendedInfoReaderHelper> {
            coJustRun { setExtendedVersionInformation(expectedValue) }
        }
        val scanner = createScanner(scannerInfoReaderMockk)
        scanner.connect(mockkInputStream, mockkOutputStream)

        scanner.setVersionInformation(expectedValue)
        coVerify { scannerInfoReaderMockk.setExtendedVersionInformation(expectedValue) }
    }

    @Test
    fun scanner_call_getCypressFirmwareVersion() = runTest {
        val expectedValue = mockk<CypressFirmwareVersion>()

        val scannerInfoReaderMockk = mockk<ScannerExtendedInfoReaderHelper> {
            coEvery { getCypressVersion() } returns expectedValue
        }
        val scanner = createScanner(scannerInfoReaderMockk)
        scanner.connect(mockkInputStream, mockkOutputStream)

        val result = scanner.getCypressFirmwareVersion()
        assertThat(result).isEqualTo(expectedValue)
    }

    @Test
    fun scanner_call_getImageQualityPreview() = runTest {
        val expectedValue = GetImageQualityPreviewResponse(1)

        val messageInputStreamMock =
            setupMainMessageInputStreamMock(un20Messages = listOf(expectedValue))
        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)

        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.state.un20On = true

        val result = scanner.getImageQualityPreview()
        assertThat(result).isEqualTo(expectedValue.imageQualityScore)
    }

    @Test(expected = NotConnectedException::class)
    fun scanner_connectThenDisconnectThenEnterMainMode_throwsException() = runTest {
        scanner.connect(mockk(), mockk())
        scanner.disconnect()
        scanner.enterMainMode()
    }

    @Test
    fun scanner_connect_callsConnectOnRootMessageStreams() = runTest {
        val rootMessageChannel: RootMessageChannel = mockk {
            justRun { connect(any(), any()) }
        }
        scanner = Scanner(
            MainMessageChannel(mockkMessageInputStream, mockkMessageOutputStream, coroutineContext),
            rootMessageChannel,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.connect(mockkInputStream, mockkOutputStream)
        verify { rootMessageChannel.connect(any(), any()) }
    }

    @Test
    fun scanner_connect_stateIsInRootMode() {
        scanner.connect(mockkInputStream, mockkOutputStream)
        assertThat(scanner.state.mode).isEqualTo(Mode.ROOT)
    }

    @Test
    fun scanner_connectThenEnterMainMode_callsConnectOnMainMessageStreams() = runTest {
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        verify { mockkMessageInputStream.connect(any()) }
        verify { mockkMessageOutputStream.connect(mockkOutputStream) }
        scanner.disconnect()
        verify { mockkMessageInputStream.disconnect() }
        verify { mockkMessageOutputStream.disconnect() }
    }

    @Test
    fun scanner_connectThenEnterMainMode_stateIsInMainMode() = runTest {
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        assertThat(scanner.state.mode).isEqualTo(Mode.MAIN)
    }

    @Test
    fun `scanner disconnect does nothing when scanner already disconnected`() {
        val mockRootMessageChannel = mockk<RootMessageChannel>()
        val mockMainMessageChannel = mockk<MainMessageChannel>()
        val mockStmOtaMessageChannel = mockk<StmOtaMessageChannel>()
        val mockCypressOtaMessageChannel = mockk<CypressOtaMessageChannel>()
        scanner = Scanner(
            mockMainMessageChannel,
            mockRootMessageChannel,
            mockk(),
            mockCypressOtaMessageChannel,
            mockStmOtaMessageChannel,
            mockk(),
            mockk(),
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.disconnect()
        verify(exactly = 0) { mockRootMessageChannel.disconnect() }
        verify(exactly = 0) { mockMainMessageChannel.disconnect() }
        verify(exactly = 0) { mockStmOtaMessageChannel.disconnect() }
        verify(exactly = 0) { mockCypressOtaMessageChannel.disconnect() }
    }

    @Test()
    fun scanner_connectThenEnterCypressOtaMode_callsConnectOnCypressOtaMessageStreams() = runTest {
        val mockCypressOtaMessageChannel =
            CypressOtaMessageChannel(mockk(relaxed = true), mockk(relaxed = true), coroutineContext)
        scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockCypressOtaMessageChannel,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterCypressOtaMode()
        verify { mockCypressOtaMessageChannel.connect(any(), any()) }
        scanner.disconnect()
        verify { mockCypressOtaMessageChannel.disconnect() }
    }

    @Test
    fun scanner_connectThenEnterCypressOtaMode_stateIsInCypressOtaMode() = runTest {
        val mockkCypressOtaMessageChannel: CypressOtaMessageChannel = mockk {
            justRun { connect(any(), any()) }
        }
        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockkCypressOtaMessageChannel,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.connect(mockkInputStream, mockkOutputStream)

        scanner.enterCypressOtaMode()

        assertThat(scanner.state.mode).isEqualTo(Mode.CYPRESS_OTA)
    }

    @Test
    fun scanner_connectThenEnterCypressOtaModeThenStartCypressOta_receivesProgressCorrectly() = runTest {
        val progressValues = listOf(0.25f, 0.50f, 0.75f, 1.00f)

        val mockkCypressOtaController = mockk<CypressOtaController> {
            coEvery {
                program(any(), any())
            } returns progressValues.asFlow()
        }

        val mockkMessageInputStream = mockk<CypressOtaMessageInputStream>(relaxed = true) {
            every { cypressOtaResponseStream } returns emptyList<CypressOtaResponse>().asFlow()
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            CypressOtaMessageChannel(
                mockkMessageInputStream,
                mockk(relaxed = true),
                coroutineContext,
            ),
            mockk(),
            mockkCypressOtaController,
            mockk(),
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterCypressOtaMode()
        val testObserver = scanner.startCypressOta(byteArrayOf())
        assertThat(testObserver.toList()).containsExactlyElementsIn(progressValues).inOrder()
    }

    @Test
    fun scanner_connectThenEnterStmOtaMode_callsConnectOnStmOtaMessageStreams() = runTest {
        val mockkMessageInputStream = mockk<StmOtaMessageInputStream> {
            justRun { connect(any()) }
            justRun { disconnect() }
            every { stmOtaResponseStream } returns emptyList<StmOtaResponse>().asFlow()
        }
        val mockkMessageOutputStream = mockk<StmOtaMessageOutputStream> {
            justRun { connect(any()) }
            justRun { disconnect() }
        }

        val mockkOutputStream = mockk<OutputStream>()

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            StmOtaMessageChannel(
                mockkMessageInputStream,
                mockkMessageOutputStream,
                coroutineContext,
            ),
            mockk(),
            mockk(),
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.connect(mockkInputStream, mockkOutputStream)

        scanner.enterStmOtaMode()

        verify { mockkMessageInputStream.connect(any()) }
        verify { mockkMessageOutputStream.connect(mockkOutputStream) }
        scanner.disconnect()
        verify { mockkMessageInputStream.disconnect() }
        verify { mockkMessageOutputStream.disconnect() }
    }

    @Test
    fun scanner_connectThenEnterStmOtaMode_stateIsInStmOtaMode() = runTest {
        val mockkMessageInputStream = mockk<StmOtaMessageInputStream> {
            justRun { connect(any()) }
            every { stmOtaResponseStream } returns emptyList<StmOtaResponse>().asFlow()
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            StmOtaMessageChannel(
                mockkMessageInputStream,
                mockk(relaxed = true),
                coroutineContext,
            ),
            mockk(),
            mockk(),
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.connect(mockkInputStream, mockkOutputStream)

        scanner.enterStmOtaMode()

        assertThat(scanner.state.mode).isEqualTo(Mode.STM_OTA)
    }

    @Test
    fun scanner_connectThenEnterStmOtaModeThenStartStmOta_receivesProgressCorrectly() = runTest {
        val progressValues = listOf(0.25f, 0.50f, 0.75f, 1.00f)

        val mockkStmOtaController = mockk<StmOtaController> {
            coEvery {
                program(any(), any())
            } returns progressValues.asFlow()
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(relaxed = true),
            mockk(),
            mockkStmOtaController,
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.connect(mockkInputStream, mockkOutputStream)

        scanner.enterStmOtaMode()

        val testObserver = scanner.startStmOta(byteArrayOf())

        assertThat(testObserver.toList()).containsExactlyElementsIn(progressValues).inOrder()
    }

    @Test(expected = IncorrectModeException::class)
    fun scanner_connectThenTurnUn20On_throwsException() = runTest {
        val scanner = createScanner(setupMainMessageInputStreamMock(), mockkMessageOutputStream)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.turnUn20On()
        assertThat(scanner.state).isNull()
    }

    @Test
    fun scannerVeroEvents_differentKindsOfEventsCreated_forwardsOnlyTriggerEventsToObservers() = runTest {
        val numberOfEvents = 3
        every { mockkMessageInputStream.veroEvents } returns flow {
            repeat(numberOfEvents) { emit(Un20StateChangeEvent(DigitalValue.TRUE)) }
            repeat(numberOfEvents) { emit(TriggerButtonPressedEvent()) }
        }
        scanner.connect(mockkInputStream, mockk())
        scanner.enterMainMode()

        val eventsCount = scanner.triggerButtonFlow.toList().count()
        assertThat(eventsCount).isEqualTo(numberOfEvents)
    }

    @Test
    fun scanner_turnOnAndOffUn20_changesStateCorrectlyUponStateChangeEvent() = runTest {
        val messageInputStreamMock = setupMainMessageInputStreamMock(
            veroMessages = listOf(
                SetUn20OnResponse(OperationResultCode.OK),
            ),
            veroEventsMessages = listOf(
                Un20StateChangeEvent(DigitalValue.TRUE),
            ),
        )

        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)

        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.turnUn20On()
        assertThat(scanner.state.un20On).isTrue()

        scanner.turnUn20Off()
        assertThat(scanner.state.un20On).isFalse()
    }

    @Test
    fun scanner_setSmileLedState_changesStateCorrectly() = runTest {
        val messageInputStreamMock = setupMainMessageInputStreamMock()

        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)

        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()

        val smileLedState = SmileLedState(
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
        )
        scanner.setSmileLedState(smileLedState)
        assertThat(scanner.state.smileLedState).isEqualTo(smileLedState)
    }

    @Test
    fun scanner_captureFingerprintWithUn20On_receivesFingerprint() = runTest {
        val captureResponse = CaptureFingerprintResponse(CaptureFingerprintResult.OK)
        val messageInputStreamMock =
            setupMainMessageInputStreamMock(un20Messages = listOf(captureResponse))
        val mockkMessageOutputStream = mockkMessageOutputStream
        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.state.un20On = true
        val result = scanner.captureFingerprint()
        assertThat(result).isEqualTo(captureResponse.captureFingerprintResult)
    }

    @Test(expected = IllegalUn20StateException::class)
    fun scanner_captureFingerprintWithUn20Off_throwsException() = runTest {
        val responseSubject = listOf(CaptureFingerprintResponse(CaptureFingerprintResult.OK))
        val messageInputStreamMock = setupMainMessageInputStreamMock(un20Messages = responseSubject)
        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.captureFingerprint()
    }

    @Test
    fun scanner_acquireTemplateWithUn20On_receivesTemplate() = runTest {
        val template = byteArrayOf(0x10, 0x20, 0x30, 0x40)
        val expectedResponseData = template
        val responseSubject =
            listOf(GetTemplateResponse(Scanner.DEFAULT_TEMPLATE_TYPE, TemplateData(template)))
        val messageInputStreamMock = setupMainMessageInputStreamMock(un20Messages = responseSubject)
        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.state.un20On = true
        val result = scanner.acquireTemplate()
        assertThat(result).isNotNull()
        assertThat(result?.template).isEqualTo(expectedResponseData)
    }

    @Test
    fun scanner_acquireImageWithUn20On_receivesImage() = runTest {
        val image = byteArrayOf(0x10, 0x20, 0x30, 0x40)
        val crcCheck = -42
        val responseSubject = listOf(GetImageResponse(ImageFormat.RAW, ImageData(image, crcCheck)))
        val messageInputStreamMock = setupMainMessageInputStreamMock(un20Messages = responseSubject)
        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.state.un20On = true

        val result = scanner.acquireImage()
        assertThat(result).isNotNull()
        assertThat(result?.image).isEqualTo(image)
    }

    @Test
    fun scanner_acquireImage_noImageTakenCallsComplete() = runTest {
        val responseSubject = listOf(GetImageResponse(ImageFormat.RAW, null))
        val messageInputStreamMock = setupMainMessageInputStreamMock(un20Messages = responseSubject)
        val scanner = createScanner(messageInputStreamMock, mockkMessageOutputStream)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.state.un20On = true
        val result = scanner.acquireImage()
        assertThat(result).isNull()
    }

    @Test
    fun scanner_connectThenDisconnect_resetsToDisconnectedState() {
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.disconnect()
        assertThat(scanner.state).isEqualTo(disconnectedScannerState())
    }

    @Test
    fun scanner_connectThenDisconnect_clearScannerInfo() {
        scanner.connect(mockkInputStream, mockkOutputStream)
        scannerInfo.setScannerId("123")
        scanner.disconnect()
        assertThat(scanner.state).isEqualTo(disconnectedScannerState())
        assertThat(scannerInfo.scannerId).isNull()
    }

    @Test
    fun scanner_getStmVersion_shouldReturnStmExtendedVersion() = runTest {
        val expectedVersion = StmExtendedFirmwareVersion("1.E-1.1")
        val scannerInfoReaderMockk = mockk<ScannerExtendedInfoReaderHelper> {
            coEvery { getStmExtendedFirmwareVersion() } returns expectedVersion
        }
        val scanner = createScanner(scannerInfoReaderMockk)
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        val result = scanner.getStmFirmwareVersion()
        assertThat(result).isEqualTo(expectedVersion)
    }

    @Test
    fun scanner_getCypressExtendedVersion_shouldReturnCypressExtendedVersion() = runTest {
        val expectedVersion = CypressExtendedFirmwareVersion("1.E-1.1")
        val scannerInfoReaderMockk = mockk<ScannerExtendedInfoReaderHelper> {
            coEvery { getCypressExtendedVersion() } returns expectedVersion
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            scannerInfoReaderMockk,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            scannerInfo,
            dispatcher,
        )
        scanner.connect(mockkInputStream, mockkOutputStream)

        val result = scanner.getCypressExtendedFirmwareVersion()
        assertThat(result).isEqualTo(expectedVersion)
    }

    @Test
    fun test_startUn20Ota() = runTest {
        val otaBinary = byteArrayOf()
        coEvery { un20OtaController.program(any(), otaBinary) } returns flowOf()
        scanner.connect(mockkInputStream, mockkOutputStream)
        scanner.enterMainMode()
        scanner.state.un20On = true
        scanner.startUn20Ota(otaBinary)
        coVerify { un20OtaController.program(any(), otaBinary) }
    }

    private fun setupMainMessageInputStreamMock(
        veroMessages: List<VeroResponse> = emptyList(),
        un20Messages: List<Un20Response> = emptyList(),
        veroEventsMessages: List<VeroEvent> = emptyList(),
    ): MainMessageInputStream = mockk {
        justRun { connect(any()) }
        justRun { disconnect() }
        every { un20Responses } returns un20Messages.asFlow()
        every { veroResponses } returns veroMessages.asFlow()
        every { veroEvents } returns veroEventsMessages.asFlow()
    }

    private fun setupRootMessageChannelMock(): RootMessageChannel {
        val mockRootMessageInputStream = mockk<RootMessageInputStream> {
            justRun { connect(any()) }
            justRun { disconnect() }
            every { rootResponseStream } returns listOf(
                EnterMainModeResponse(),
                EnterStmOtaModeResponse(),
                EnterCypressOtaModeResponse(),
            ).asFlow()
        }
        val mockRootMessageOutputStream = mockk<RootMessageOutputStream> {
            justRun { connect(any()) }
            justRun { disconnect() }
            justRun { sendMessage(any()) }
        }
        return RootMessageChannel(
            mockRootMessageInputStream,
            mockRootMessageOutputStream,
            coroutineContext,
        )
    }

    private fun createScanner(
        messageInputStreamMock: MainMessageInputStream,
        mockkMessageOutputStream: MainMessageOutputStream,
    ) = Scanner(
        MainMessageChannel(messageInputStreamMock, mockkMessageOutputStream, coroutineContext),
        setupRootMessageChannelMock(),
        mockk(),
        mockk(),
        mockk(),
        mockk(),
        mockk(),
        mockk(),
        scannerInfo,
        dispatcher,
    )

    private fun createScanner(scannerInfoReaderMockk: ScannerExtendedInfoReaderHelper) = Scanner(
        MainMessageChannel(mockkMessageInputStream, mockkMessageOutputStream, coroutineContext),
        setupRootMessageChannelMock(),
        scannerInfoReaderMockk,
        mockk(),
        mockk(),
        mockk(),
        mockk(),
        mockk(),
        scannerInfo,
        dispatcher,
    )
}

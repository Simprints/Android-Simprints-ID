package com.simprints.feature.externalcredential.screens.scandocument

import android.nfc.Tag
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipAccessProtocol
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadError
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadException
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadFailure
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadResult
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzData
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzFormat
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzParser
import com.simprints.feature.externalcredential.screens.scandocument.usecase.ReadDocumentChipUseCase
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrText
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ReadTextFromImageUseCase
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import com.simprints.infra.camera.Frame
import com.simprints.infra.camera.postprocess.FrameCropToTargetUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ExternalCredentialsDocumentScanViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var frameCropToTarget: FrameCropToTargetUseCase

    @MockK
    private lateinit var readTextFromImage: ReadTextFromImageUseCase

    @MockK
    private lateinit var mrzParser: MrzParser

    @MockK
    private lateinit var readDocumentChip: ReadDocumentChipUseCase

    private val frame = mockk<Frame>(relaxed = true)
    private val tag = mockk<Tag>()

    private lateinit var viewModel: ExternalCredentialsDocumentScanViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() } returns Timestamp(1L)
        every { readTextFromImage(any()) } returns OcrText(allLines = emptyList())

        viewModel = ExternalCredentialsDocumentScanViewModel(
            timeHelper = timeHelper,
            frameCropToTarget = frameCropToTarget,
            readTextFromImage = readTextFromImage,
            mrzParser = mrzParser,
            readDocumentChip = readDocumentChip,
            bgDispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `initial state is NotInitialised`() {
        assertThat(viewModel.stateLiveData.test().value()).isEqualTo(DocumentScanState.NotInitialised)
    }

    @Test
    fun `camera permission status is mapped to the state`() {
        val observer = viewModel.stateLiveData.test()

        viewModel.updateCameraPermissionStatus(PermissionStatus.Denied)
        assertThat(observer.value()).isEqualTo(DocumentScanState.NoCameraPermission(shouldOpenPhoneSettings = false))

        viewModel.updateCameraPermissionStatus(PermissionStatus.DeniedNeverAskAgain)
        assertThat(observer.value()).isEqualTo(DocumentScanState.NoCameraPermission(shouldOpenPhoneSettings = true))

        viewModel.updateCameraPermissionStatus(PermissionStatus.Granted)
        assertThat(observer.value()).isEqualTo(DocumentScanState.ScanningMrz)
    }

    @Test
    fun `camera permission updates are ignored once MRZ is read`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()

        viewModel.updateCameraPermissionStatus(PermissionStatus.Granted)

        assertThat(observer.value()).isEqualTo(DocumentScanState.WaitingForChip(OCR_MRZ))
    }

    @Test
    fun `frames are ignored before scanning starts`() = runTest {
        viewModel.processFrame(frame)

        verify(exactly = 0) { readTextFromImage(any()) }
    }

    @Test
    fun `single MRZ read does not complete the scan`() = runTest {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCameraPermissionStatus(PermissionStatus.Granted)
        every { mrzParser.parse(any()) } returns OCR_MRZ

        viewModel.processFrame(frame)

        assertThat(observer.value()).isEqualTo(DocumentScanState.ScanningMrz)
    }

    @Test
    fun `two consecutive identical MRZ reads complete the scan`() = runTest {
        val observer = viewModel.stateLiveData.test()

        readMrz()

        assertThat(observer.value()).isEqualTo(DocumentScanState.WaitingForChip(OCR_MRZ))
    }

    @Test
    fun `different MRZ reads restart the consecutive read count`() = runTest {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCameraPermissionStatus(PermissionStatus.Granted)
        val otherMrz = OCR_MRZ.copy(documentNumber = "OTHER1234")

        every { mrzParser.parse(any()) } returnsMany listOf(OCR_MRZ, otherMrz, OCR_MRZ)
        repeat(3) { viewModel.processFrame(frame) }
        assertThat(observer.value()).isEqualTo(DocumentScanState.ScanningMrz)

        every { mrzParser.parse(any()) } returns OCR_MRZ
        viewModel.processFrame(frame)
        assertThat(observer.value()).isEqualTo(DocumentScanState.WaitingForChip(OCR_MRZ))
    }

    @Test
    fun `frames without text do not change the state`() = runTest {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCameraPermissionStatus(PermissionStatus.Granted)
        every { readTextFromImage(any()) } returns null

        viewModel.processFrame(frame)

        assertThat(observer.value()).isEqualTo(DocumentScanState.ScanningMrz)
        verify(exactly = 0) { mrzParser.parse(any()) }
    }

    @Test
    fun `rescan returns to MRZ scanning and requires new consecutive reads`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()

        viewModel.rescanMrz()
        assertThat(observer.value()).isEqualTo(DocumentScanState.ScanningMrz)

        viewModel.processFrame(frame)
        assertThat(observer.value()).isEqualTo(DocumentScanState.ScanningMrz)
    }

    @Test
    fun `tag is ignored when not waiting for chip`() = runTest {
        viewModel.updateCameraPermissionStatus(PermissionStatus.Granted)

        viewModel.onTagDiscovered(tag)

        verify(exactly = 0) { readDocumentChip(any(), any()) }
    }

    @Test
    fun `successful chip read moves to ChipRead`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()
        every { readDocumentChip(tag, OCR_MRZ) } returns CHIP_RESULT
        every { timeHelper.now() } returns Timestamp(2L)

        viewModel.onTagDiscovered(tag)

        assertThat(observer.value()).isEqualTo(
            DocumentScanState.ChipRead(
                mrz = OCR_MRZ,
                chip = CHIP_RESULT,
                scanStartTime = Timestamp(1L),
                scanEndTime = Timestamp(2L),
            ),
        )
    }

    @Test
    fun `failed chip read returns to waiting with the error and the read log`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()
        val log = listOf("BAC failed: wrong access key (SW 6300)")
        every { readDocumentChip(any(), any()) } throws ChipReadException(ChipReadError.AccessDenied, log = log)

        viewModel.onTagDiscovered(tag)

        assertThat(observer.value()).isEqualTo(
            DocumentScanState.WaitingForChip(
                mrz = OCR_MRZ,
                lastFailure = ChipReadFailure(ChipReadError.AccessDenied, attempt = 1, log = log),
            ),
        )
    }

    @Test
    fun `unexpected chip read failure returns to waiting with generic error`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()
        every { readDocumentChip(any(), any()) } throws IllegalStateException("secret")

        viewModel.onTagDiscovered(tag)

        val failure = (observer.value() as DocumentScanState.WaitingForChip).lastFailure!!
        assertThat(failure.error).isEqualTo(ChipReadError.ReadFailed)
        assertThat(failure.log).containsExactly("Unexpected error: java.lang.IllegalStateException")
    }

    @Test
    fun `reading state contains the attempt number`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()
        every { readDocumentChip(any(), any()) } throws ChipReadException(ChipReadError.ConnectionLost) andThen CHIP_RESULT

        viewModel.onTagDiscovered(tag)
        viewModel.onTagDiscovered(tag)

        assertThat(observer.valueHistory()).containsAtLeast(
            DocumentScanState.ReadingChip(OCR_MRZ, attempt = 1),
            DocumentScanState.ReadingChip(OCR_MRZ, attempt = 2),
        ).inOrder()
    }

    @Test
    fun `chip read can be retried after failure`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()
        every { readDocumentChip(any(), any()) } throws ChipReadException(ChipReadError.ConnectionLost) andThen CHIP_RESULT

        viewModel.onTagDiscovered(tag)
        viewModel.onTagDiscovered(tag)

        assertThat(observer.value()).isInstanceOf(DocumentScanState.ChipRead::class.java)
    }

    @Test
    fun `retry clears the last failure`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()
        every { readDocumentChip(any(), any()) } throws ChipReadException(ChipReadError.ConnectionLost)
        viewModel.onTagDiscovered(tag)

        viewModel.retryChipRead()

        assertThat(observer.value()).isEqualTo(DocumentScanState.WaitingForChip(OCR_MRZ))
    }

    @Test
    fun `retry is ignored when there is no failure`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()
        val historySize = observer.valueHistory().size

        viewModel.retryChipRead()

        assertThat(observer.valueHistory()).hasSize(historySize)
    }

    @Test
    fun `rescan resets the attempt counter`() = runTest {
        val observer = viewModel.stateLiveData.test()
        readMrz()
        every { readDocumentChip(any(), any()) } throws ChipReadException(ChipReadError.ConnectionLost)
        viewModel.onTagDiscovered(tag)

        viewModel.rescanMrz()
        readMrz()
        viewModel.onTagDiscovered(tag)

        assertThat((observer.value() as DocumentScanState.WaitingForChip).lastFailure?.attempt).isEqualTo(1)
    }

    @Test
    fun `scanned credential result is not available before chip is read`() = runTest {
        readMrz()

        assertThat(viewModel.buildScannedCredentialResult()).isNull()
    }

    @Test
    fun `scanned credential result contains only the document number read from the chip`() = runTest {
        readMrz()
        every { readDocumentChip(any(), any()) } returns CHIP_RESULT
        viewModel.onTagDiscovered(tag)

        val result = viewModel.buildScannedCredentialResult()!!

        assertThat(result.document).isEqualTo(MfidDocument.IcaoDocument(credential = "CHIP12345".asTokenizableRaw()))
        assertThat(result.documentImagePath).isNull()
        assertThat(result.zoomedCredentialImagePath).isNull()
        assertThat(result.credentialBoundingBox).isNull()
        assertThat(result.scanStartTime).isEqualTo(Timestamp(1L))
    }

    private suspend fun readMrz() {
        viewModel.updateCameraPermissionStatus(PermissionStatus.Granted)
        every { mrzParser.parse(any()) } returns OCR_MRZ
        repeat(2) { viewModel.processFrame(frame) }
    }

    companion object {
        private val OCR_MRZ = MrzData(
            format = MrzFormat.TD3,
            documentCode = "P<",
            issuingState = "UTO",
            documentNumber = "L898902C3",
            dateOfBirth = "740812",
            sex = "F",
            dateOfExpiry = "120415",
            nationality = "UTO",
            primaryIdentifier = "ERIKSSON",
            secondaryIdentifier = "ANNA MARIA",
            optionalData1 = "ZE184226B",
            optionalData2 = "",
        )

        // Different document number to verify that the chip value takes precedence over the OCR one
        private val CHIP_RESULT = ChipReadResult(
            accessProtocol = ChipAccessProtocol.PACE,
            ldsVersion = "0107",
            unicodeVersion = "040000",
            dataGroupsInCom = listOf(1, 2),
            dataGroupsInSod = listOf(1, 2),
            sodDigestAlgorithm = "SHA-256",
            dg1HashMatchesSod = true,
            documentSignerSubject = null,
            documentSignerIssuer = null,
            mrz = OCR_MRZ.copy(documentNumber = "CHIP12345"),
        )
    }
}

package com.simprints.feature.externalcredential.screens.scandocument

import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherBG
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadError
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadException
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadFailure
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzData
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzParser
import com.simprints.feature.externalcredential.screens.scandocument.usecase.ReadDocumentChipUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ReadTextFromImageUseCase
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.infra.camera.Frame
import com.simprints.infra.camera.postprocess.FrameCropToTargetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * PoC: reads an ICAO 9303 document by first reading the MRZ via camera OCR and then using it as an access key to read the NFC chip.
 *
 * All data read from the document is kept in memory only (no [androidx.lifecycle.SavedStateHandle], no logging), and only the document
 * number leaves this screen.
 */
@HiltViewModel
internal class ExternalCredentialsDocumentScanViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val frameCropToTarget: FrameCropToTargetUseCase,
    private val readTextFromImage: ReadTextFromImageUseCase,
    private val mrzParser: MrzParser,
    private val readDocumentChip: ReadDocumentChipUseCase,
    @param:DispatcherBG private val bgDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private var scanStartTime: Timestamp? = null
    private var lastMrzRead: MrzData? = null
    private var consecutiveMrzReads = 0
    private val isReadingChip = AtomicBoolean(false)

    @Volatile
    private var chipReadAttempts = 0

    @Volatile
    private var state: DocumentScanState = DocumentScanState.NotInitialised
        set(value) {
            field = value
            stateLiveData.postValue(value)
        }

    val stateLiveData: LiveData<DocumentScanState>
        field = MutableLiveData<DocumentScanState>(DocumentScanState.NotInitialised)

    fun updateCameraPermissionStatus(permissionStatus: PermissionStatus) {
        // Permission is only relevant while the MRZ has not been read yet
        if (state !is DocumentScanState.NotInitialised && state !is DocumentScanState.NoCameraPermission) return

        state = when (permissionStatus) {
            PermissionStatus.Granted -> {
                scanStartTime = timeHelper.now()
                DocumentScanState.ScanningMrz
            }

            PermissionStatus.Denied -> DocumentScanState.NoCameraPermission(shouldOpenPhoneSettings = false)
            PermissionStatus.DeniedNeverAskAgain -> DocumentScanState.NoCameraPermission(shouldOpenPhoneSettings = true)
        }
    }

    /**
     * Runs OCR on the frame and moves on to chip reading once the same valid MRZ is read [REQUIRED_CONSECUTIVE_MRZ_READS] times in a row.
     * Check digits make a wrong read very unlikely, repeated reads rule out the remaining OCR flukes.
     */
    suspend fun processFrame(frame: Frame) {
        if (state !is DocumentScanState.ScanningMrz) return

        val mrz = withContext(bgDispatcher) {
            val ocrText = readTextFromImage(frameCropToTarget(frame)) ?: return@withContext null
            mrzParser.parse(ocrText.allLines.map { it.text })
        }
        if (mrz == null || state !is DocumentScanState.ScanningMrz) return

        if (mrz == lastMrzRead) {
            consecutiveMrzReads++
        } else {
            lastMrzRead = mrz
            consecutiveMrzReads = 1
        }
        if (consecutiveMrzReads >= REQUIRED_CONSECUTIVE_MRZ_READS) {
            resetMrzReads()
            state = DocumentScanState.WaitingForChip(mrz)
        }
    }

    fun rescanMrz() {
        resetMrzReads()
        chipReadAttempts = 0
        state = DocumentScanState.ScanningMrz
    }

    /**
     * Clears the last failure so that the next discovered tag is read again. The screen also restarts the NFC reader mode, so that a
     * document that is still held against the phone is discovered again without having to move it away.
     */
    fun retryChipRead() {
        val waitingState = state as? DocumentScanState.WaitingForChip ?: return
        if (waitingState.lastFailure == null) return
        state = waitingState.copy(lastFailure = null)
    }

    /** Called from the NFC reader mode callback (binder thread) */
    fun onTagDiscovered(tag: Tag) {
        val waitingState = state as? DocumentScanState.WaitingForChip ?: return
        if (!isReadingChip.compareAndSet(false, true)) return

        val attempt = ++chipReadAttempts
        state = DocumentScanState.ReadingChip(waitingState.mrz, attempt)
        viewModelScope.launch {
            try {
                val chip = withContext(bgDispatcher) { readDocumentChip(tag, waitingState.mrz) }
                state = DocumentScanState.ChipRead(
                    mrz = waitingState.mrz,
                    chip = chip,
                    scanStartTime = scanStartTime ?: timeHelper.now(),
                    scanEndTime = timeHelper.now(),
                )
            } catch (e: ChipReadException) {
                state = DocumentScanState.WaitingForChip(waitingState.mrz, ChipReadFailure(e.error, attempt, e.log))
            } catch (e: Exception) {
                // Only the type, the message might contain the access key
                val log = listOf("Unexpected error: ${e.javaClass.name}")
                state = DocumentScanState.WaitingForChip(waitingState.mrz, ChipReadFailure(ChipReadError.ReadFailed, attempt, log))
            } finally {
                isReadingChip.set(false)
            }
        }
    }

    /**
     * Builds the result in the same shape as the other external credential scanners. Only the document number read from the chip
     * (which is authoritative over the OCR read) is passed on, no images are stored.
     */
    fun buildScannedCredentialResult(): ScannedCredentialResult? {
        val chipRead = state as? DocumentScanState.ChipRead ?: return null
        return ScannedCredentialResult(
            document = MfidDocument.IcaoDocument(credential = chipRead.chip.mrz.documentNumber.asTokenizableRaw()),
            documentImagePath = null,
            zoomedCredentialImagePath = null,
            credentialBoundingBox = null,
            scanStartTime = chipRead.scanStartTime,
            scanEndTime = chipRead.scanEndTime,
        )
    }

    override fun onCleared() {
        // Drop all references to the document data
        resetMrzReads()
        state = DocumentScanState.NotInitialised
        super.onCleared()
    }

    private fun resetMrzReads() {
        lastMrzRead = null
        consecutiveMrzReads = 0
    }

    private companion object {
        const val REQUIRED_CONSECUTIVE_MRZ_READS = 2
    }
}

package com.simprints.feature.externalcredential.screens.scandocument

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.extensions.getCurrentPermissionStatus
import com.simprints.core.tools.extensions.hasPermission
import com.simprints.core.tools.extensions.permissionFromResult
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialsDocumentScanBinding
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadError
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzData
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetBoundsRelativeToParentUseCase
import com.simprints.infra.camera.CameraFrameProvider
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.awaitLayout
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Calendar
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

/**
 * PoC: ICAO 9303 document reading - MRZ via camera OCR, then BAC/PACE protected data from the NFC chip.
 *
 * UI texts are intentionally hardcoded and not localised.
 */
@AndroidEntryPoint
@ExcludedFromGeneratedTestCoverageReports("UI class")
internal class ExternalCredentialsDocumentScanFragment : Fragment(R.layout.fragment_external_credentials_document_scan) {
    private val binding by viewBinding(FragmentExternalCredentialsDocumentScanBinding::bind)
    private val viewModel by viewModels<ExternalCredentialsDocumentScanViewModel>()

    @Inject
    lateinit var cameraFrameProvider: CameraFrameProvider

    @Inject
    lateinit var getBoundsRelativeToParentUseCase: GetBoundsRelativeToParentUseCase

    private val cameraInitLock = Mutex()
    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(requireContext()) }
    private var isReaderModeEnabled = false
    private var pendingReaderModeJob: Job? = null

    private val cameraManager: CameraManager? by lazy { requireContext().getSystemService(CameraManager::class.java) }
    private val camerasInUse = mutableSetOf<String>()
    private val isAnyCameraInUse = MutableStateFlow(false)

    /** Tracks camera usage by any client (including this screen) - called on the main thread */
    private val cameraAvailabilityCallback = object : CameraManager.AvailabilityCallback() {
        override fun onCameraAvailable(cameraId: String) {
            camerasInUse.remove(cameraId)
            isAnyCameraInUse.value = camerasInUse.isNotEmpty()
        }

        override fun onCameraUnavailable(cameraId: String) {
            camerasInUse.add(cameraId)
            isAnyCameraInUse.value = true
        }
    }

    private val launchPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.updateCameraPermissionStatus(requireActivity().permissionFromResult(CAMERA, granted))
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ExternalCredentialsDocumentScanFragment started", tag = MULTI_FACTOR_ID)

        viewModel.stateLiveData.observe(viewLifecycleOwner, ::renderState)
        startFrameProcessing()

        if (!requireActivity().hasPermission(CAMERA)) {
            launchPermissionRequest.launch(CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()
        // Initial availability of every camera is reported right after the registration
        cameraManager?.registerAvailabilityCallback(cameraAvailabilityCallback, Handler(Looper.getMainLooper()))
        viewModel.updateCameraPermissionStatus(requireActivity().getCurrentPermissionStatus(CAMERA))
        // NFC might have been toggled in the system settings while the screen was paused
        viewModel.stateLiveData.value?.let(::renderState)
    }

    override fun onPause() {
        disableReaderMode()
        cameraManager?.unregisterAvailabilityCallback(cameraAvailabilityCallback)
        camerasInUse.clear()
        isAnyCameraInUse.value = false
        super.onPause()
    }

    override fun onDestroyView() {
        stopCamera()
        super.onDestroyView()
    }

    private fun renderState(state: DocumentScanState) {
        when (state) {
            DocumentScanState.NotInitialised -> renderMrzScanning(startCamera = false)
            is DocumentScanState.NoCameraPermission -> renderNoPermission(state.shouldOpenPhoneSettings)
            DocumentScanState.ScanningMrz -> renderMrzScanning(startCamera = true)
            is DocumentScanState.WaitingForChip -> renderWaitingForChip(state)
            is DocumentScanState.ReadingChip -> renderReadingChip(state)
            is DocumentScanState.ChipRead -> renderChipRead(state)
        }
        updateReaderMode(state)
    }

    // region MRZ scanning

    private fun renderMrzScanning(startCamera: Boolean) = with(binding) {
        setCameraViewsVisible(true)
        setChipViewsVisible(false)
        permissionRequestView.isVisible = false
        mrzInstructionsText.text = "Place the machine readable zone (the lines with <<<) inside the frame"
        buttonPrimary.isVisible = false
        buttonSecondary.isVisible = false
        if (startCamera) initCamera()
    }

    private fun renderNoPermission(shouldOpenPhoneSettings: Boolean) = with(binding) {
        mrzInstructionsText.isVisible = false
        setChipViewsVisible(false)
        buttonPrimary.isVisible = false
        buttonSecondary.isVisible = false
        val bodyText = getString(IDR.string.mfid_scan_camera_permission_body, getString(IDR.string.mfid_type_icao_document))
        if (shouldOpenPhoneSettings) {
            permissionRequestView.init(
                title = IDR.string.face_capture_permission_denied,
                body = bodyText,
                buttonText = IDR.string.fingerprint_connect_phone_settings_button,
                onClickListener = {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${requireActivity().packageName}".toUri()))
                },
            )
        } else {
            permissionRequestView.init(
                title = IDR.string.face_capture_permission_denied,
                body = bodyText,
                buttonText = IDR.string.face_capture_permission_action,
                onClickListener = { launchPermissionRequest.launch(CAMERA) },
            )
        }
        permissionRequestView.isVisible = true
    }

    private fun startFrameProcessing() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                cameraFrameProvider.frames.collect { frame -> viewModel.processFrame(frame) }
            }
        }
    }

    private fun initCamera() {
        viewLifecycleOwner.lifecycleScope.launch {
            cameraInitLock.withLock {
                if (!cameraFrameProvider.isInitialised()) {
                    binding.documentScanPreview.awaitLayout()
                    binding.documentScanArea.awaitLayout()
                    cameraFrameProvider.initialiseCamera(
                        lifecycleOwner = viewLifecycleOwner,
                        cameraPreviewView = binding.documentScanPreview,
                        target = getBoundsRelativeToParentUseCase(parent = binding.documentScanPreview, child = binding.documentScanArea),
                    ) { e ->
                        Simber.e("Camera not available for document MRZ scanning", e, tag = MULTI_FACTOR_ID)
                        Toast.makeText(requireActivity(), "No camera", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun stopCamera() {
        if (cameraFrameProvider.isInitialised()) {
            cameraFrameProvider.release()
        }
    }

    // endregion

    // region Chip reading

    private fun renderWaitingForChip(state: DocumentScanState.WaitingForChip) = with(binding) {
        stopCamera()
        setCameraViewsVisible(false)
        setChipViewsVisible(true)
        chipReadingProgress.isVisible = false
        resultText.text = buildString {
            append(formatOcrMrz(state.mrz))
            state.lastFailure?.let { failure ->
                appendLine()
                appendLine()
                appendLine("CHIP READ LOG (attempt ${failure.attempt}, ${failure.error})")
                append(failure.log.joinToString("\n"))
            }
        }

        val adapter = nfcAdapter
        when {
            adapter == null -> {
                chipStatusText.text = "This device does not support NFC, the document chip cannot be read"
                buttonPrimary.isVisible = false
            }

            !adapter.isEnabled -> {
                chipStatusText.text = "NFC is turned off. Turn it on to read the document chip."
                buttonPrimary.isVisible = true
                buttonPrimary.text = "Open NFC settings"
                buttonPrimary.setOnClickListener { startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
            }

            state.lastFailure != null -> {
                chipStatusText.text = "Attempt ${state.lastFailure.attempt} failed: ${state.lastFailure.error.toMessage()}\n\n" +
                    "Keep the document flat against the back of the phone and tap Retry, or move it away and place it again. " +
                    "See the log below for details."
                buttonPrimary.isVisible = true
                buttonPrimary.text = "Retry chip read"
                buttonPrimary.setOnClickListener {
                    viewModel.retryChipRead()
                    restartReaderMode()
                }
            }

            else -> {
                chipStatusText.text = "Hold the document flat against the back of the phone and keep it still"
                buttonPrimary.isVisible = true
                buttonPrimary.text = "Restart NFC reader"
                buttonPrimary.setOnClickListener { restartReaderMode() }
            }
        }
        buttonSecondary.isVisible = true
        buttonSecondary.text = "Rescan MRZ"
        buttonSecondary.setOnClickListener { viewModel.rescanMrz() }
    }

    private fun renderReadingChip(state: DocumentScanState.ReadingChip) = with(binding) {
        setCameraViewsVisible(false)
        setChipViewsVisible(true)
        chipStatusText.text = "Reading the chip (attempt ${state.attempt}), keep the document still…"
        chipReadingProgress.isVisible = true
        resultText.text = formatOcrMrz(state.mrz)
        buttonPrimary.isVisible = false
        buttonSecondary.isVisible = false
    }

    private fun renderChipRead(state: DocumentScanState.ChipRead) = with(binding) {
        setCameraViewsVisible(false)
        setChipViewsVisible(true)
        chipReadingProgress.isVisible = false
        chipStatusText.text = "Document chip read successfully"
        resultText.text = DocumentScanResultFormatter.format(
            ocrMrz = state.mrz,
            chip = state.chip,
            currentYear = Calendar.getInstance().get(Calendar.YEAR),
        )
        buttonSecondary.isVisible = true
        buttonSecondary.text = "Rescan MRZ"
        buttonSecondary.setOnClickListener { viewModel.rescanMrz() }
        buttonPrimary.isVisible = true
        buttonPrimary.setText(IDR.string.mfid_continue)
        buttonPrimary.setOnClickListener { finishWithDocumentNumber() }
    }

    /**
     * Returns the document number the same way as the other scanners - by passing the scanned credential result to the search screen.
     * Unlike the other scanners, the capture attempt is intentionally not recorded, so that no capture event with the document data is
     * persisted.
     */
    private fun finishWithDocumentNumber() {
        val result = viewModel.buildScannedCredentialResult() ?: return
        findNavController().navigateSafely(
            this,
            ExternalCredentialsDocumentScanFragmentDirections.actionExternalCredentialsDocumentScanToExternalCredentialSearch(result),
        )
    }

    /**
     * Reader mode is kept on while waiting for and reading the chip. It takes precedence over any other NFC dispatch, and is bound to the
     * resumed activity.
     *
     * It is only enabled once no camera is in use: some devices (e.g. Samsung) force-disable NFC polling while a camera is open, which
     * silently drops the reader mode. The camera is released asynchronously when the MRZ is read, so enabling the reader mode right away
     * would leave the chip to the system NFC dispatch.
     */
    private fun updateReaderMode(state: DocumentScanState) {
        // isResumed instead of the lifecycle state - the lifecycle only becomes RESUMED after onResume returns, so checking it from
        // onResume would never re-enable the reader mode after the screen was paused (e.g. returning from the NFC settings)
        val shouldBeEnabled = (state is DocumentScanState.WaitingForChip || state is DocumentScanState.ReadingChip) && isResumed
        binding.nfcReaderStatusText.isVisible = shouldBeEnabled
        if (!shouldBeEnabled) {
            disableReaderMode()
            return
        }
        if (isReaderModeEnabled || pendingReaderModeJob?.isActive == true) return
        if (nfcAdapter?.isEnabled != true) {
            binding.nfcReaderStatusText.isVisible = false
            return
        }

        binding.nfcReaderStatusText.text = "NFC reader: waiting for the camera to be released…"
        pendingReaderModeJob = viewLifecycleOwner.lifecycleScope.launch {
            val isCameraReleased = withTimeoutOrNull(CAMERA_RELEASE_TIMEOUT_MS) { awaitCamerasReleased() } != null
            if (!isCameraReleased) {
                Simber.i("Camera still in use, enabling NFC reader mode anyway", tag = MULTI_FACTOR_ID)
            }
            enableReaderMode(isCameraReleased)
        }
    }

    /** Waits until no camera has been in use for [NFC_SETTLE_DELAY_MS], so that the NFC service has also processed the camera release */
    private suspend fun awaitCamerasReleased() {
        while (true) {
            isAnyCameraInUse.first { !it }
            delay(NFC_SETTLE_DELAY_MS)
            if (!isAnyCameraInUse.value) return
        }
    }

    private fun enableReaderMode(isCameraReleased: Boolean) {
        val adapter = nfcAdapter ?: return
        if (isReaderModeEnabled || !adapter.isEnabled || !isResumed) return
        adapter.enableReaderMode(
            requireActivity(),
            viewModel::onTagDiscovered,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            // The default presence check (every ~125ms) interferes with the slow document chip commands (PACE/BAC, file reads)
            // and makes the chip stop responding on some devices
            Bundle().apply { putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, NFC_PRESENCE_CHECK_DELAY_MS) },
        )
        isReaderModeEnabled = true
        Simber.i("NFC reader mode enabled", tag = MULTI_FACTOR_ID)
        binding.nfcReaderStatusText.text = if (isCameraReleased) {
            "NFC reader: active"
        } else {
            "NFC reader: active, but a camera is still in use - if the chip is not detected, tap Restart NFC reader"
        }
    }

    /**
     * Restarting the reader mode makes the NFC controller poll again, so a document that is still in the field is discovered again
     * (Android does not report the same tag twice while it stays in the field). It also recovers the reader mode if the system dropped it.
     */
    private fun restartReaderMode() {
        disableReaderMode()
        viewModel.stateLiveData.value?.let(::updateReaderMode)
    }

    private fun disableReaderMode() {
        pendingReaderModeJob?.cancel()
        pendingReaderModeJob = null
        if (!isReaderModeEnabled) return
        activity?.let { nfcAdapter?.disableReaderMode(it) }
        isReaderModeEnabled = false
    }

    // endregion

    private fun setCameraViewsVisible(isVisible: Boolean) = with(binding) {
        documentScanPreview.isVisible = isVisible
        viewfinderMask.isVisible = isVisible
        documentScanArea.isVisible = isVisible
        mrzInstructionsText.isVisible = isVisible
    }

    private fun setChipViewsVisible(isVisible: Boolean) = with(binding) {
        chipStatusText.isVisible = isVisible
        resultScroll.isVisible = isVisible
        if (!isVisible) chipReadingProgress.isVisible = false
    }

    private fun formatOcrMrz(mrz: MrzData) = buildString {
        appendLine("MRZ read by camera (chip access key):")
        appendLine("Document number: ${mrz.documentNumber}")
        appendLine("Date of birth (YYMMDD): ${mrz.dateOfBirth}")
        append("Date of expiry (YYMMDD): ${mrz.dateOfExpiry}")
    }

    private fun ChipReadError.toMessage() = when (this) {
        ChipReadError.TagNotSupported -> "This NFC tag is not a document chip."
        ChipReadError.ConnectionLost -> "Connection to the chip was lost."
        ChipReadError.AccessDenied -> "The chip rejected the access key. If retrying does not help, rescan the MRZ."
        ChipReadError.ReadFailed -> "Reading the chip failed. If it keeps failing at the same step, see the log."
    }

    private companion object {
        const val CAMERA_RELEASE_TIMEOUT_MS = 5_000L
        const val NFC_SETTLE_DELAY_MS = 500L
        const val NFC_PRESENCE_CHECK_DELAY_MS = 2_000
    }
}

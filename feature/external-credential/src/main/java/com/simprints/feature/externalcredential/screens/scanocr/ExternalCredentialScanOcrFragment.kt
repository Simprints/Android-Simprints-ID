package com.simprints.feature.externalcredential.screens.scanocr

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialScanOcrBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.usecase.BuildOcrCropConfigUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CaptureFrameUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ProvideCameraListenerUseCase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ExternalCredentialScanOcrFragment : Fragment(R.layout.fragment_external_credential_scan_ocr) {
    private val args: ExternalCredentialScanOcrFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentExternalCredentialScanOcrBinding::bind)
    private val mainViewModel: ExternalCredentialViewModel by activityViewModels()
    private val viewModel by viewModels<ExternalCredentialScanOcrViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return viewModelFactory.create(args.ocrDocumentType) as T
            }
        }
    }

    private val launchPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val cameraPermissionStatus = requireActivity().permissionFromResult(CAMERA, granted)
        viewModel.updateCameraPermissionStatus(cameraPermissionStatus)
    }
    private var isCameraInitialized = false
    private var isOcrStarted = false
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalysis: ImageAnalysis
    private val successfulCaptureRequired = 5
    private val framesToSkip = 10

    @Inject
    lateinit var viewModelFactory: ExternalCredentialScanOcrViewModel.Factory

    @Inject
    lateinit var captureFrameUseCase: CaptureFrameUseCase

    @Inject
    lateinit var buildOcrCropConfigUseCase: BuildOcrCropConfigUseCase

    @Inject
    lateinit var provideCameraListenerUseCase: ProvideCameraListenerUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        args.ocrDocumentType
        Simber.i("ExternalCredentialScanOcrFragment started", tag = MULTI_FACTOR_ID)

        initObservers()

        if (!requireActivity().hasPermission(CAMERA)) {
            launchPermissionRequest.launch(CAMERA)
        }
    }

    override fun onDestroy() {
        stopOcr()
        super.onDestroy()
    }

    private fun initObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScanOcrState.ReadyToScan -> {
                    renderInitialState(state)
                    initCamera()
                }

                is ScanOcrState.InProgress -> {
                    startOcr(framesToSkip = framesToSkip)
                    renderProgress(state)
                    if (state.successfulCaptures >= successfulCaptureRequired) {
                        stopOcr()
                        viewModel.processOcrResultsAndFinish()
                    }
                }

                is ScanOcrState.NoCameraPermission -> renderNoPermission(state)
            }
        }

        viewModel.finishOcrEvent.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                stopOcr()
                findNavController().navigateSafely(
                    this@ExternalCredentialScanOcrFragment,
                    R.id.action_externalCredentialScanOcr_to_externalCredentialSearch
                )
            }
        )
    }

    private fun initCamera() {
        if (isCameraInitialized) return
        isCameraInitialized = true
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        val cameraListener = provideCameraListenerUseCase(
            cameraProviderFuture = cameraProviderFuture,
            surfaceProvider = binding.preview.surfaceProvider,
            viewLifecycleOwner = viewLifecycleOwner,
            onImageAnalysisReady = { imageAnalysis = it }
        )
        cameraProviderFuture.addListener(cameraListener, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun renderProgress(state: ScanOcrState.InProgress) = with(binding) {
        val progressPercentage = (state.successfulCaptures * 100 / successfulCaptureRequired).coerceAtMost(100)
        buttonScan.isVisible = false
        qrInstructionsText.isVisible = false
        progressCard.isVisible = true
        progressBar.progress = progressPercentage
    }

    private fun renderInitialState(state: ScanOcrState.ReadyToScan) = with(binding) {
        val documentTypeText = viewModel.getDocumentTypeRes(state.ocrDocumentType).run(::getString)
        permissionRequestView.isVisible = false
        qrInstructionsText.isVisible = true
        qrInstructionsText.text = getString(IDR.string.mfid_scan_instructions, documentTypeText)
        progressCard.isVisible = false
        buttonScan.isVisible = true
        buttonScan.setOnClickListener {
            viewModel.startOcr()
        }
    }

    private fun renderNoPermission(state: ScanOcrState.NoCameraPermission) = with(binding) {
        qrInstructionsText.isVisible = false
        buttonScan.isVisible = false
        val documentTypeText = viewModel.getDocumentTypeRes(state.ocrDocumentType).run(::getString)
        val bodyText = getString(IDR.string.mfid_scan_camera_permission_body, documentTypeText)
        if (state.shouldOpenPhoneSettings) {
            permissionRequestView.init(
                title = IDR.string.face_capture_permission_denied,
                body = bodyText,
                buttonText = IDR.string.fingerprint_connect_phone_settings_button,
                onClickListener = {
                    requireActivity().startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            "package:${requireActivity().packageName}".toUri(),
                        ),
                    )
                }
            )
        } else {
            permissionRequestView.init(
                title = IDR.string.face_capture_permission_denied,
                body = bodyText,
                buttonText = IDR.string.face_capture_permission_action,
                onClickListener = {
                    launchPermissionRequest.launch(CAMERA)
                }
            )
        }
        permissionRequestView.isVisible = true
    }

    private fun startOcr(framesToSkip: Int) {
        if (isOcrStarted) return
        isOcrStarted = true
        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            lifecycleScope.launch(Dispatchers.IO) {
                val (bitmap, imageInfo) = captureFrameUseCase(imageProxy, framesToSkip) ?: return@launch
                val cropConfig: OcrCropConfig = buildOcrCropConfigUseCase(
                    rotationDegrees = imageInfo.rotationDegrees,
                    cameraPreview = binding.preview,
                    documentScannerArea = binding.documentScannerArea
                )
                withContext(Dispatchers.Main) {
                    viewModel.runOcrOnFrame(frame = bitmap, cropConfig)
                }
            }
        }
    }

    private fun stopOcr() {
        imageAnalysis.clearAnalyzer()
        isOcrStarted = false
    }
}

package com.simprints.feature.externalcredential.screens.scanocr

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.DispatcherBG
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.getCurrentPermissionStatus
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialScanOcrBinding
import com.simprints.feature.externalcredential.ext.animateIn
import com.simprints.feature.externalcredential.ext.animateOut
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.model.mapToCredentialType
import com.simprints.feature.externalcredential.screens.scanocr.usecase.BuildOcrCropConfigUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ProvideCameraListenerUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
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
        previousPermissionStatus = cameraPermissionStatus
        if (cameraPermissionStatus == PermissionStatus.Granted) {
            initializeFragment()
        } else {
            val shouldOpenPhoneSettings = cameraPermissionStatus == PermissionStatus.DeniedNeverAskAgain
            renderNoPermission(shouldOpenPhoneSettings)
        }
    }
    private var previousPermissionStatus: PermissionStatus? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalysis: ImageAnalysis
    private var progressAnimator: ViewPropertyAnimator? = null
    private var checkAnimator: ViewPropertyAnimator? = null
    private var isAnimatingCompletion: Boolean = false
    private var pendingFinishAction: (() -> Unit)? = null

    @Inject
    lateinit var viewModelFactory: ExternalCredentialScanOcrViewModel.Factory

    @Inject
    lateinit var buildOcrCropConfigUseCase: BuildOcrCropConfigUseCase

    @Inject
    lateinit var provideCameraListenerUseCase: ProvideCameraListenerUseCase

    @Inject
    @DispatcherBG
    lateinit var bgDispatcher: CoroutineDispatcher

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ExternalCredentialScanOcrFragment started", tag = MULTI_FACTOR_ID)
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        val currentPermission = requireActivity().getCurrentPermissionStatus(CAMERA)
        when (currentPermission) {
            PermissionStatus.Granted -> initializeFragment()
            PermissionStatus.Denied -> {
                // Permission dialog was already displayed, and user denied permissions. Showing rationale so to avoid constantly-appearing
                // system dialog.
                if (previousPermissionStatus == currentPermission) {
                    renderNoPermission(shouldOpenPhoneSettings = false)
                } else {
                    launchPermissionRequest.launch(CAMERA)
                }
            }

            PermissionStatus.DeniedNeverAskAgain -> {
                // Requesting system dialog just in case. Some devices faulty report 'DeniedNeverAskAgain' status when it is actually 'Denied'
                launchPermissionRequest.launch(CAMERA)
                renderNoPermission(shouldOpenPhoneSettings = true)
            }
        }

    }

    override fun onDestroy() {
        stopOcr()
        stopCamera()
        clearAnimations()
        super.onDestroy()
    }

    private fun clearAnimations() {
        pendingFinishAction = null
        isAnimatingCompletion = false
        checkAnimator?.cancel()
        progressAnimator?.cancel()
    }

    private fun initializeFragment() {
        renderInitialState()
        initCamera(onComplete = {
            if (viewModel.isOcrActive) {
                startOcr()
            }
        })

    }

    private fun initObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScanOcrState.ScanningInProgress -> {
                    renderProgress(state)
                    if (state.successfulCaptures >= state.scansRequired) {
                        stopOcr()
                        viewModel.processOcrResultsAndFinish()
                    }
                }

                ScanOcrState.NotScanning -> renderInitialState()
                ScanOcrState.Complete -> animateCompletionState()
            }
        }

        viewModel.finishOcrEvent.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { detectedBlock ->
                finish(detectedBlock)
            }
        )
    }

    private fun initCamera(onComplete: () -> Unit) {
        if (::cameraExecutor.isInitialized) {
            return
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        val cameraListener = provideCameraListenerUseCase(
            cameraProviderFuture = cameraProviderFuture,
            surfaceProvider = binding.preview.surfaceProvider,
            viewLifecycleOwner = viewLifecycleOwner,
            onImageAnalysisReady = {
                imageAnalysis = it
                onComplete()
            }
        )
        cameraProviderFuture.addListener(cameraListener, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun renderProgress(state: ScanOcrState.ScanningInProgress) = with(binding) {
        val progressPercentage = (state.successfulCaptures * 100 / state.scansRequired).coerceAtMost(100)
        buttonScan.isVisible = false
        progressContainer.isVisible = true
        progressBar.isVisible = true
        iconScanComplete.alpha = 0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(progressPercentage, true)
        } else {
            progressBar.progress = progressPercentage
        }
        instructionsText.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_black))
        viewfinderMask.setMaskColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_white))
        viewfinderMask.alpha = 0.9f
    }

    private fun renderInitialState() = with(binding) {
        val documentTypeText = viewModel.getDocumentTypeRes().run(::getString)
        permissionRequestView.isVisible = false
        instructionsText.isVisible = true
        instructionsText.text = getString(IDR.string.mfid_scan_instructions, documentTypeText)
        instructionsText.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_white))
        documentScannerArea.isVisible = true
        progressContainer.isVisible = false
        buttonScan.isVisible = true
        buttonScan.setOnClickListener {
            viewModel.ocrStarted()
            startOcr()
        }
        viewfinderMask.setMaskColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_black))
        viewfinderMask.alpha = 0.5f
    }

    private fun animateCompletionState() = with(binding) {
        val duration = 300L
        isAnimatingCompletion = true
        progressBar.animateOut(duration, scaleX = true, fragment = this@ExternalCredentialScanOcrFragment)
        scanInstructions.animateOut(duration, scaleX = false, fragment = this@ExternalCredentialScanOcrFragment)
        iconScanComplete.animateIn(duration, fragment = this@ExternalCredentialScanOcrFragment, onComplete = {
            isAnimatingCompletion = false
            // Execute any pending action after the animation. Currently used is for next fragment navigation
            pendingFinishAction?.invoke()
            pendingFinishAction = null
        })
    }

    private fun renderNoPermission(shouldOpenPhoneSettings: Boolean) {
        with(binding) {
            instructionsText.isVisible = false
            progressContainer.isVisible = false
            documentScannerArea.isInvisible = true
            buttonScan.isVisible = false
            val documentTypeText = viewModel.getDocumentTypeRes().run(::getString)
            val bodyText = getString(IDR.string.mfid_scan_camera_permission_body, documentTypeText)
            if (shouldOpenPhoneSettings) {
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
    }

    private fun startOcr() {
        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            if (viewModel.isRunningOcrOnFrame) {
                imageProxy.close()
                return@setAnalyzer
            }
            viewModel.ocrOnFrameStarted()
            lifecycleScope.launch(bgDispatcher) {
                try {
                    val (bitmap, imageInfo) = imageProxy.toBitmap() to imageProxy.imageInfo
                    val cropConfig: OcrCropConfig = buildOcrCropConfigUseCase(
                        rotationDegrees = imageInfo.rotationDegrees,
                        cameraPreview = binding.preview,
                        documentScannerArea = binding.documentScannerArea
                    )
                    viewModel.runOcrOnFrame(frame = bitmap, cropConfig)
                } finally {
                    imageProxy.close()
                }
            }
        }
    }

    private fun stopCamera() {
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    private fun stopOcr() {
        if (::imageAnalysis.isInitialized) {
            imageAnalysis.clearAnalyzer()
        }
    }

    private fun finish(detectedBlock: DetectedOcrBlock) {
        val navigationAction = {
            val credentialType = detectedBlock.documentType.mapToCredentialType()
            val args = ScannedCredential(
                credential = detectedBlock.readoutValue,
                credentialType = credentialType,
                previewImagePath = detectedBlock.imagePath,
                imageBoundingBox = detectedBlock.blockBoundingBox
            )
            findNavController().navigateSafely(
                this@ExternalCredentialScanOcrFragment,
                ExternalCredentialScanOcrFragmentDirections.actionExternalCredentialScanOcrToExternalCredentialSearch(args)
            )
        }
        if (isAnimatingCompletion) {
            pendingFinishAction = navigationAction
        } else {
            navigationAction.invoke()
        }
    }

}

package com.simprints.feature.externalcredential.screens.scanocr

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import com.simprints.feature.externalcredential.screens.scanocr.usecase.BuildOcrCropConfigUseCase
import com.simprints.feature.externalcredential.screens.scanocr.usecase.ProvideCameraListenerUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.fadeIn
import com.simprints.infra.uibase.view.fadeOut
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
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
    private lateinit var imageCapture: ImageCapture
    private var progressAnimator: ViewPropertyAnimator? = null
    private var checkAnimator: ViewPropertyAnimator? = null
    private var isAnimatingCompletion: Boolean = false
    private var pendingFinishAction: (() -> Unit)? = null
    private var ocrPreProcessingJob: Job? = null

    @Inject
    lateinit var viewModelFactory: ExternalCredentialScanOcrViewModel.Factory

    @Inject
    lateinit var buildOcrCropConfigUseCase: BuildOcrCropConfigUseCase

    @Inject
    lateinit var provideCameraListenerUseCase: ProvideCameraListenerUseCase

    @Inject
    @DispatcherBG
    lateinit var bgDispatcher: CoroutineDispatcher

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ExternalCredentialScanOcrFragment started", tag = MULTI_FACTOR_ID)
    }

    override fun onResume() {
        super.onResume()
        when (val currentPermission = requireActivity().getCurrentPermissionStatus(CAMERA)) {
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

    override fun onDestroyView() {
        stopOcr()
        stopCamera()
        clearAnimations()
        super.onDestroyView()
    }

    private fun clearAnimations() {
        pendingFinishAction = null
        isAnimatingCompletion = false
        checkAnimator?.cancel()
        progressAnimator?.cancel()
    }

    private fun initializeFragment() {
        initObservers()
        initCamera(onComplete = {
            if (viewModel.isOcrActive) {
                startOcr()
            }
        })
    }

    private fun initObservers() {
        viewModel.scanOcrStateLiveData.observe(viewLifecycleOwner) { state ->
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
            LiveDataEventWithContentObserver { scannedCredential ->
                scheduleFinish(scannedCredential)
            },
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
            onImageAnalysisReady = { analysis ->
                imageAnalysis = analysis
                onComplete()
            },
            onImageCaptureReady = { capture ->
                imageCapture = capture
            },
        )
        cameraProviderFuture.addListener(cameraListener, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun renderProgress(state: ScanOcrState.ScanningInProgress) = with(binding) {
        val progressPercentage = (state.successfulCaptures * 100 / state.scansRequired).coerceAtMost(100)
        buttonScan.isVisible = false
        progressContainer.isVisible = true
        progressBar.isVisible = true
        iconScanComplete.alpha = 0f
        progressBar.setProgressCompat(progressPercentage, true)
        instructionsText.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_black))
        viewfinderMask.setMaskColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_white))
        viewfinderMask.alpha = VIEW_FINDER_ALPHA_SCAN_ACTIVE
    }

    private fun renderInitialState() = with(binding) {
        val documentTypeText = viewModel.getDocumentTypeRes().run(::getString)
        permissionRequestView.isVisible = false
        instructionsText.isVisible = true
        instructionsText.text = getString(IDR.string.mfid_scan_instructions, documentTypeText)
        instructionsText.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_white))
        documentScannerArea.isVisible = true
        progressContainer.isInvisible = true
        buttonScan.isVisible = true
        buttonScan.setOnClickListener {
            viewModel.ocrStarted()
            startOcr()
        }
        viewfinderMask.setMaskColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_black))
        viewfinderMask.alpha = VIEW_FINDER_ALPHA_INITIAL
    }

    private fun animateCompletionState() = with(binding) {
        isAnimatingCompletion = true
        val finalVisibility = View.INVISIBLE
        progressBar.fadeOut(
            FINISH_ANIMATION_DURATION,
            scaleX = true,
            fragment = this@ExternalCredentialScanOcrFragment,
            finalVisibility = finalVisibility,
        )
        scanInstructions.fadeOut(
            FINISH_ANIMATION_DURATION,
            scaleX = false,
            fragment = this@ExternalCredentialScanOcrFragment,
            finalVisibility = finalVisibility,
        )
        iconScanComplete.fadeIn(FINISH_ANIMATION_DURATION, fragment = this@ExternalCredentialScanOcrFragment, onComplete = {
            isAnimatingCompletion = false
            // Execute any pending action after the animation. Currently used is for next fragment navigation
            pendingFinishAction?.invoke()
            pendingFinishAction = null
        })
    }

    private fun renderNoPermission(shouldOpenPhoneSettings: Boolean) {
        with(binding) {
            instructionsText.isVisible = false
            progressContainer.isInvisible = true
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
                    },
                )
            } else {
                permissionRequestView.init(
                    title = IDR.string.face_capture_permission_denied,
                    body = bodyText,
                    buttonText = IDR.string.face_capture_permission_action,
                    onClickListener = {
                        launchPermissionRequest.launch(CAMERA)
                    },
                )
            }
            permissionRequestView.isVisible = true
        }
    }

    private fun startOcr() {
        imageAnalysis.setAnalyzer(cameraExecutor) { videoFrame: ImageProxy ->
            if (viewModel.isRunningOcrOnFrame.get()) {
                videoFrame.close()
                return@setAnalyzer
            }

            // Running OCR as often as we can while camera feedback is displayed to the user
            viewModel.ocrOnFrameStarted()
            if (viewModel.ocrConfig.useHighRes) {
                videoFrame.close()
                captureHighResImageForOcr { highResImage ->
                    preProcessImageAndRunOcr(highResImage)
                }
            } else {
                preProcessImageAndRunOcr(videoFrame)
            }
        }
    }

    private fun preProcessImageAndRunOcr(imageProxy: ImageProxy) {
        ocrPreProcessingJob?.cancel()
        ocrPreProcessingJob = lifecycleScope.launch(bgDispatcher) {
            try {
                val (bitmap, imageInfo) = imageProxy.toBitmap() to imageProxy.imageInfo
                if (ocrPreProcessingJob?.isActive == true) {
                    val cropConfig: OcrCropConfig = buildOcrCropConfigUseCase(
                        rotationDegrees = imageInfo.rotationDegrees,
                        cameraPreview = binding.preview,
                        documentScannerArea = binding.documentScannerArea,
                    )
                    viewModel.runOcrOnFrame(frame = bitmap, cropConfig)
                } else {
                    Simber.i(
                        "Unable to run OCR preprocessing, coroutine context is cancelled",
                        tag = MULTI_FACTOR_ID,
                    )
                }
            } finally {
                imageProxy.close()
            }
        }
    }

    private fun captureHighResImageForOcr(onImageCaptured: (ImageProxy) -> Unit) {
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    onImageCaptured(imageProxy)
                }

                override fun onError(e: ImageCaptureException) {
                    Simber.e("Photo capture failed in OCR", e, MULTI_FACTOR_ID)
                }
            },
        )
    }

    private fun stopCamera() {
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    private fun stopOcr() {
        ocrPreProcessingJob?.cancel()
        if (::imageAnalysis.isInitialized) {
            imageAnalysis.clearAnalyzer()
        }
        viewModel.ocrStopped()
    }

    /**
     * Waits until all animations are complete before navigating away. Completion animations are in place because the execution of
     * [ExternalCredentialScanOcrViewModel.processOcrResultsAndFinish] is not immediate, and it makes the transition to the next fragment
     * smoother for user.
     *
     * The animation state is stored in the [isAnimatingCompletion]. If it is set to true, the navigation action is set to
     * [pendingFinishAction] which will be executed once animations are complete. If false, the navigation will proceed immediately.
     */
    private fun scheduleFinish(credential: ScannedCredential) {
        val navigationAction = {
            findNavController().navigateSafely(
                this@ExternalCredentialScanOcrFragment,
                ExternalCredentialScanOcrFragmentDirections.actionExternalCredentialScanOcrToExternalCredentialSearch(credential),
            )
        }
        if (isAnimatingCompletion) {
            pendingFinishAction = navigationAction
        } else {
            navigationAction.invoke()
        }
    }

    companion object {
        private const val VIEW_FINDER_ALPHA_INITIAL = 0.5f
        private const val VIEW_FINDER_ALPHA_SCAN_ACTIVE = 0.9f
        private const val FINISH_ANIMATION_DURATION = 300L
    }
}

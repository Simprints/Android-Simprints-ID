package com.simprints.feature.externalcredential.screens.scanocr

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.DispatcherBG
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extensions.getCurrentPermissionStatus
import com.simprints.core.tools.extensions.permissionFromResult
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialScanOcrBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.scanocr.model.LightingConditionsAssessment
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrConfig
import com.simprints.feature.externalcredential.screens.scanocr.usecase.GetBoundsRelativeToParentUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.infra.camera.CameraFrameProvider
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.camera.qrscan.CameraFocusManager
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.awaitLayout
import com.simprints.infra.uibase.view.fadeIn
import com.simprints.infra.uibase.view.fadeOut
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
        if (cameraPermissionStatus == PermissionStatus.Granted) {
            initializeFragment()
        } else {
            val shouldOpenPhoneSettings = cameraPermissionStatus == PermissionStatus.DeniedNeverAskAgain
            renderNoPermission(shouldOpenPhoneSettings)
        }
    }
    private var shouldAutoRequestCameraPermission: Boolean = true
    private var isAnimatingCompletion: Boolean = false
    private var pendingFinishAction: (() -> Unit)? = null

    private val cameraInitLock = Mutex()

    @Inject
    lateinit var cameraFrameProvider: CameraFrameProvider

    @Inject
    lateinit var getBoundsRelativeToParentUseCase: GetBoundsRelativeToParentUseCase

    @Inject
    lateinit var viewModelFactory: ExternalCredentialScanOcrViewModel.Factory

    @Inject
    lateinit var cameraFocusManagerFactory: CameraFocusManager.Factory

    @Inject
    @DispatcherBG
    lateinit var bgDispatcher: CoroutineDispatcher

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)

        initObservers()
        setUpFrameProcessing()
        Simber.i("ExternalCredentialScanOcrFragment started", tag = MULTI_FACTOR_ID)
    }

    override fun onResume() {
        super.onResume()
        when (val currentPermission = requireActivity().getCurrentPermissionStatus(CAMERA)) {
            PermissionStatus.Granted -> initializeFragment()
            PermissionStatus.Denied, PermissionStatus.DeniedNeverAskAgain -> if (shouldAutoRequestCameraPermission) {
                requestCameraPermission()
            } else {
                renderNoPermission(shouldOpenPhoneSettings = currentPermission == PermissionStatus.DeniedNeverAskAgain)
            }
        }
    }

    override fun onDestroyView() {
        stopCamera()
        clearAnimations()
        super.onDestroyView()
    }

    private fun clearAnimations() {
        pendingFinishAction = null
        isAnimatingCompletion = false
    }

    private fun initializeFragment() = viewLifecycleOwner.lifecycleScope.launch {
        val ocrConfig = viewModel.awaitOcrConfig()
        cameraInitLock.withLock {
            if (!cameraFrameProvider.isInitialised()) {
                initCamera(ocrConfig)
            }
        }
        renderInitialState()
    }

    private fun initObservers() {
        viewModel.lightingConditionsAssessment.observe(viewLifecycleOwner) { lightingConditionsAssessment ->
            val lightingConditionsHintTextResourceId = when (lightingConditionsAssessment) {
                LightingConditionsAssessment.NORMAL -> IDR.string.mfid_scan_hint_lighting_normal
                LightingConditionsAssessment.TOO_DIM -> IDR.string.mfid_scan_hint_lighting_too_dim
                LightingConditionsAssessment.TOO_BRIGHT -> IDR.string.mfid_scan_hint_lighting_too_bright
            }
            binding.scanHint.setText(lightingConditionsHintTextResourceId)
        }

        viewModel.scanOcrStateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScanOcrState.ScanningInProgress -> {
                    renderProgress(state)
                    if (state.successfulCaptures >= state.scansRequired) {
                        // Animate progress bar to go to 100%, and then finish the flow
                        binding.captureProgress.setProgressAnimated(
                            100,
                            durationMs = PROGRESS_FINISH_REMAINING_MS,
                            onComplete = {
                                viewModel.processOcrResultsAndFinish()
                            },
                            interpolator = AccelerateInterpolator(),
                        )
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

    private suspend fun initCamera(ocrConfig: OcrConfig) {
        if (cameraFrameProvider.isInitialised()) {
            return
        }
        // Wait for the views to be properly laid out
        binding.preview.awaitLayout()
        binding.documentScannerArea.awaitLayout()

        val targetRect = getBoundsRelativeToParentUseCase(
            parent = binding.preview,
            child = binding.documentScannerArea,
        )
        cameraFrameProvider.initialiseCamera(
            lifecycleOwner = viewLifecycleOwner,
            previewView = binding.preview,
            target = targetRect,
            highResolution = ocrConfig.useHighRes,
        ) {
            Simber.e("Camera binding failed in OCR", it, MULTI_FACTOR_ID)
        }
    }

    private fun renderProgress(state: ScanOcrState.ScanningInProgress) = with(binding) {
        if (!captureProgress.isAnimating) {
            captureProgress.setProgressAnimated(
                95,
                durationMs = PROGRESS_ONE_SEGMENT_MS * state.scansRequired,
                interpolator = DecelerateInterpolator(),
            )
        }
        buttonScan.isVisible = false
        progressContainer.isVisible = true
        captureProgress.isVisible = true
        documentScannerArea.isVisible = true
        iconScanComplete.alpha = 0f
        instructionsText.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_black))
        viewfinderMask.maskColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_white)
        viewfinderMask.alpha = VIEW_FINDER_ALPHA_SCAN_ACTIVE
        scanHint.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_black))
        scanHint.isVisible = true
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
            viewModel.startScanning()
        }
        viewfinderMask.maskColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_black)
        viewfinderMask.alpha = VIEW_FINDER_ALPHA_INITIAL
        scanHint.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_white))
        scanHint.isVisible = true
    }

    private fun animateCompletionState() = with(binding) {
        isAnimatingCompletion = true
        val finalVisibility = View.INVISIBLE
        captureProgress.fadeOut(
            FINISH_ANIMATION_DURATION,
            fragment = this@ExternalCredentialScanOcrFragment,
            finalVisibility = finalVisibility,
        )
        scanInstructions.fadeOut(
            FINISH_ANIMATION_DURATION,
            fragment = this@ExternalCredentialScanOcrFragment,
            finalVisibility = finalVisibility,
        )
        iconScanComplete.fadeIn(FINISH_ANIMATION_DURATION, fragment = this@ExternalCredentialScanOcrFragment, onComplete = {
            isAnimatingCompletion = false
            // Execute any pending action after the animation. Currently used is for next fragment navigation
            pendingFinishAction?.invoke()
            pendingFinishAction = null
        })
        scanHint.isInvisible = true
    }

    private fun renderNoPermission(shouldOpenPhoneSettings: Boolean) {
        stopCamera()
        with(binding) {
            instructionsText.isVisible = false
            progressContainer.isInvisible = true
            documentScannerArea.isInvisible = true
            scanHint.isInvisible = true
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
                        requestCameraPermission()
                    },
                )
            }
            permissionRequestView.isVisible = true
        }
    }

    private fun requestCameraPermission() {
        shouldAutoRequestCameraPermission = false
        launchPermissionRequest.launch(CAMERA)
    }

    private fun setUpFrameProcessing() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewModel.isProcessingImage.collect { isProcessing ->
                        cameraFrameProvider.setFrameEmissionEnabled(!isProcessing)
                    }
                }

                launch {
                    cameraFrameProvider.frames.collect { frame ->
                        viewModel.imageProcessingStarted()
                        viewModel.processImage(frame)
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

    /**
     * Waits until all animations are complete before navigating away. Completion animations are in place because the execution of
     * [ExternalCredentialScanOcrViewModel.processOcrResultsAndFinish] is not immediate, and it makes the transition to the next fragment
     * smoother for user.
     *
     * The animation state is stored in the [isAnimatingCompletion]. If it is set to true, the navigation action is set to
     * [pendingFinishAction] which will be executed once animations are complete. If false, the navigation will proceed immediately.
     */
    private fun scheduleFinish(credential: ScannedCredentialResult) {
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

        /**
         * How long does 1 scan iteration last.
         * According to our analytics, a single 'take photo + run OCR' operation takes around 4 seconds on average
         */
        private const val PROGRESS_ONE_SEGMENT_MS = 4000L

        /**
         * How long to display the finishing animation of the progress indicator
         */
        private const val PROGRESS_FINISH_REMAINING_MS = 500L
    }
}

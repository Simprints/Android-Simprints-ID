package com.simprints.face.capture.screens.livefeedback

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simprints.core.DispatcherBG
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.tools.extensions.getCurrentPermissionStatus
import com.simprints.core.tools.extensions.hasCameraFlash
import com.simprints.core.tools.extensions.permissionFromResult
import com.simprints.face.capture.R
import com.simprints.face.capture.databinding.FragmentLiveFeedbackBinding
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.screens.FaceCaptureViewModel
import com.simprints.infra.camera.CameraFrameProvider
import com.simprints.infra.camera.postprocess.FrameCropToTargetUseCase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.awaitLayout
import com.simprints.infra.uibase.view.setCheckedWithLeftDrawable
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

/**
 * As the user is capturing subject's face, they are presented with this fragment, which displays
 * live information about distance and whether the face is ready to be captured or not.
 * It also displays the capture process of the face and then sends this result to
 * [com.simprints.face.capture.screens.confirmation.ConfirmationFragment]
 */
@AndroidEntryPoint
internal class LiveFeedbackFragment : Fragment(R.layout.fragment_live_feedback) {
    private val mainVm: FaceCaptureViewModel by activityViewModels()

    private val vm: LiveFeedbackViewModel by viewModels()
    private val binding by viewBinding(FragmentLiveFeedbackBinding::bind)

    @Inject
    lateinit var cameraFrameProvider: CameraFrameProvider

    @Inject
    lateinit var frameCropToTargetUseCase: FrameCropToTargetUseCase

    @Inject
    @DispatcherBG
    lateinit var bgDispatcher: CoroutineDispatcher

    private var finishedHandled = false

    private val validCaptureProgressColor: Int
        get() = ContextCompat.getColor(requireContext(), IDR.color.simprints_green_light)
    private val defaultCaptureProgressColor: Int
        get() = ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey_light)
    private val validationProgressColor: Int
        get() = ContextCompat.getColor(requireContext(), IDR.color.simprints_orange)

    private val launchPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        vm.onPermissionResult(requireActivity().permissionFromResult(Manifest.permission.CAMERA, granted))
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)

        Simber.i("LiveFeedbackFragment started", tag = ORCHESTRATION)
        initFragment()
    }

    private fun initFragment() {
        bindViewModel()
        bindPermissionActions()
        setUpFrameProcessing()

        binding.captureProgress.max = 1 // normalized progress

        setUpCaptureButton()
        viewLifecycleOwner.lifecycleScope.launch {
            vm.initAutoCapture()
        }

        // Wait till the views gets its final size then init frame processor and setup the camera
        binding.faceCaptureCamera.post {
            if (view != null) {
                vm.initCapture(
                    bioSdk = mainVm.bioSDK,
                    samplesToCapture = mainVm.samplesToCapture,
                )
            }
        }

        binding.captureInstructionsBtn.setOnClickListener {
            findNavController().navigateSafely(
                currentFragment = this,
                directions = LiveFeedbackFragmentDirections.actionFaceLiveFeedbackFragmentToFacePreparationFragment(),
            )
        }
        binding.captureFeedbackPermissionButton.setOnClickListener {
            vm.onPermissionButtonClicked()
        }

        with(binding.captureFlashButton) {
            isVisible = requireContext().hasCameraFlash
            isSelected = false
            setOnClickListener {
                val torchEnabled = !binding.captureFlashButton.isSelected
                toggleTorch(torchEnabled)
            }
        }
    }

    private fun setUpCaptureButton() {
        binding.captureFeedbackBtn.setOnClickListener {
            vm.startCapture()
            toggleCaptureButtonIfAutoCapture(false)
        }
        toggleCaptureButtonIfAutoCapture(true)
    }

    private fun toggleTorch(enabled: Boolean) {
        cameraFrameProvider.setTorchEnabled(enabled)
        binding.captureFlashButton.isSelected = enabled
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera(state: LiveFeedbackState) = viewLifecycleOwner.lifecycleScope.launch {
        if (cameraFrameProvider.isInitialised()) {
            return@launch
        }

        val isTracking = state.isFaceTrackingEnabled

        // Wait for the views to be properly laid out
        binding.faceCaptureCamera.awaitLayout()
        if (!isTracking) binding.captureOverlay.awaitLayout()

        cameraFrameProvider.initialiseCamera(
            lifecycleOwner = viewLifecycleOwner,
            cameraPreviewView = binding.faceCaptureCamera,
            // Tracking analyses the whole preview; otherwise only the fixed cutout is analysed
            target = if (isTracking) null else binding.captureOverlay.circleRect.toRect(),
        )
        Simber.i("Camera setup finished", tag = FACE_CAPTURE)
    }

    override fun onPause() {
        vm.holdOffAutoCapture()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        vm.onScreenResumed(requireActivity().getCurrentPermissionStatus(Manifest.permission.CAMERA))
    }

    private fun toggleCaptureButtonIfAutoCapture(enabled: Boolean) {
        if (vm.isAutoCapture) {
            binding.captureFeedbackBtn.isClickable = enabled
        }
    }

    override fun onStop() {
        toggleTorch(false)
        super.onStop()
    }

    override fun onDestroyView() {
        cameraFrameProvider.release()
        super.onDestroyView()
    }

    private fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.state.collect(::render) }
                launch {
                    vm.state
                        .filter { it.stateInitialised }
                        .distinctUntilChangedBy { it.permissionStatus }
                        .collect { state ->
                            if (state.permissionStatus == PermissionStatus.Granted) {
                                setUpCamera(state)
                                toggleCaptureButtonIfAutoCapture(true)
                            }
                        }
                }
            }
        }
    }

    private fun bindPermissionActions() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.permissionActions.collect { action ->
                    when (action) {
                        LiveFeedbackViewModel.PermissionAction.RequestCameraPermission ->
                            launchPermissionRequest.launch(Manifest.permission.CAMERA)

                        LiveFeedbackViewModel.PermissionAction.OpenAppSettings ->
                            requireActivity().startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    "package:${requireActivity().packageName}".toUri(),
                                ),
                            )
                    }
                }
            }
        }
    }

    private fun setUpFrameProcessing() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                cameraFrameProvider.frames.collect { frame ->
                    cameraFrameProvider.setFrameEmissionEnabled(false)
                    try {
                        withContext(bgDispatcher) {
                            val cropped = frameCropToTargetUseCase(frame)
                            vm.process(originalBitmap = frame.bitmap, croppedBitmap = cropped)
                        }
                    } catch (e: CancellationException) {
                        Simber.e("Image analysis cancelled", e, tag = FACE_CAPTURE)
                        throw e
                    } catch (t: Throwable) {
                        Simber.e("Image analysis crashed", t, tag = FACE_CAPTURE)
                        // submitError updates LiveData, so ensure it happens on the main thread
                        viewLifecycleOwner.lifecycleScope.launch { mainVm.submitError(t) }
                    } finally {
                        cameraFrameProvider.setFrameEmissionEnabled(true)
                    }
                }
            }
        }
    }

    private fun render(state: LiveFeedbackState) {
        // Which mode's views exist at all is only known once the configuration has been read, so
        // both sets stay hidden until then rather than one being drawn and swapped for the other.
        // Everything below is mode-independent and renders from the first state, so a slow config
        // read still leaves a screen that can explain itself.
        if (state.stateInitialised) {
            applyCaptureMode(state.isFaceTrackingEnabled)
            // The two modes own entirely separate views, so this is the only place they are told apart
            if (state.isFaceTrackingEnabled) renderTrackingOverlay(state) else renderCutoutOverlay(state)
        }

        if (state.permissionStatus != PermissionStatus.Granted) {
            renderNoPermission()
            return
        }

        when (state.phase) {
            LiveFeedbackState.Phase.NOT_STARTED -> {
                renderControls(explanationVisible = false)
                renderFeedbackOnButton(state)
            }
            LiveFeedbackState.Phase.CAPTURING -> {
                renderControls(explanationVisible = true)
                renderFeedbackOnButton(state)
            }
            LiveFeedbackState.Phase.VALIDATING -> {
                renderControls(explanationVisible = true)
                renderValidating()
            }
            LiveFeedbackState.Phase.VALIDATION_FAILED -> {
                renderControls(explanationVisible = true)
                renderValidationFailed()
            }
            LiveFeedbackState.Phase.FINISHED -> onCaptureFinished(state.result)
        }
    }

    /**
     * Shows the views belonging to the active mode and hides the other mode's entirely, so nothing
     * from the experimental path can be drawn while the flag is off.
     */
    private fun applyCaptureMode(isFaceTrackingEnabled: Boolean) = with(binding) {
        captureOverlay.isVisible = !isFaceTrackingEnabled
        captureProgress.isVisible = !isFaceTrackingEnabled
        faceTrackingOverlay.isVisible = isFaceTrackingEnabled
        captureControlsScrim.isVisible = isFaceTrackingEnabled
    }

    private fun renderTrackingOverlay(state: LiveFeedbackState) = with(binding) {
        val hasPermission = state.permissionStatus == PermissionStatus.Granted
        faceTrackingOverlay.update(
            target = state.targetBox.takeIf { hasPermission },
            progress = if (hasPermission) state.progress else Progress.HIDDEN,
            // Only useful while the operator is still framing the subject
            showAimGuide = hasPermission &&
                (
                    state.phase == LiveFeedbackState.Phase.NOT_STARTED ||
                        state.phase == LiveFeedbackState.Phase.CAPTURING
                ),
        )
    }

    private fun renderCutoutOverlay(state: LiveFeedbackState) = with(binding) {
        val dimPreview = state.permissionStatus == PermissionStatus.Granted && state.phase != LiveFeedbackState.Phase.NOT_STARTED
        if (dimPreview) {
            captureOverlay.drawWhiteTarget()
            captureFeedbackTxtExplanation.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey))
        } else {
            captureOverlay.drawSemiTransparentTarget()
            captureFeedbackTxtExplanation.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_white))
        }

        captureProgress.value = state.progress.value
        captureProgress.progressColor = when (state.progress.tint) {
            Progress.Tint.DEFAULT -> defaultCaptureProgressColor
            Progress.Tint.VALID -> validCaptureProgressColor
            Progress.Tint.VALIDATION -> validationProgressColor
        }
        captureProgress.isInvisible = !state.progress.visible
    }

    private fun renderControls(explanationVisible: Boolean) = with(binding) {
        captureFeedbackTxtExplanation.isVisible = explanationVisible
        captureFeedbackBtn.isVisible = true
        captureFeedbackPermissionButton.isGone = true
    }

    private fun renderFeedbackOnButton(state: LiveFeedbackState) = with(binding) {
        val feedback = if (state.isAutoCapture && vm.isAutoCaptureHeldOff) {
            LiveFeedbackState.Feedback.NONE
        } else {
            state.feedback
        }

        when (feedback) {
            LiveFeedbackState.Feedback.NONE -> when {
                state.phase == LiveFeedbackState.Phase.CAPTURING ->
                    captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)

                state.isAutoCapture -> {
                    captureFeedbackBtn.setText(IDR.string.face_capture_start_capture)
                    captureFeedbackBtn.isChecked = true
                    captureFeedbackBtn.isClickable = true
                }

                else -> captureFeedbackBtn.setText(IDR.string.face_capture_title_previewing)
            }

            LiveFeedbackState.Feedback.NO_FACE ->
                renderInvalidFace(IDR.string.face_capture_title_no_face, IDR.string.face_capture_error_no_face)

            LiveFeedbackState.Feedback.LOOK_STRAIGHT ->
                renderInvalidFace(IDR.string.face_capture_title_look_straight, IDR.string.face_capture_error_look_straight)

            LiveFeedbackState.Feedback.TOO_CLOSE ->
                renderInvalidFace(IDR.string.face_capture_title_too_close, IDR.string.face_capture_error_too_close)

            LiveFeedbackState.Feedback.TOO_FAR ->
                renderInvalidFace(IDR.string.face_capture_title_too_far, IDR.string.face_capture_error_too_far)

            LiveFeedbackState.Feedback.BAD_QUALITY ->
                renderInvalidFace(IDR.string.face_capture_title_bad_quality, IDR.string.face_capture_error_bad_quality)

            LiveFeedbackState.Feedback.VALID -> {
                if (state.isAutoCapture) {
                    captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
                } else {
                    captureFeedbackBtn.setText(IDR.string.face_capture_begin_button)
                    setManualCaptureButtonClickable(true)
                }
                captureFeedbackTxtExplanation.text = null
                captureFeedbackBtn.setCheckedWithLeftDrawable(
                    true,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp),
                )
            }

            LiveFeedbackState.Feedback.VALID_CAPTURING -> {
                captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
                captureFeedbackTxtExplanation.setText(IDR.string.face_capture_hold)
                captureFeedbackBtn.setCheckedWithLeftDrawable(
                    true,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp),
                )
            }
        }
    }

    private fun renderInvalidFace(
        @StringRes titleRes: Int,
        @StringRes explanationRes: Int,
    ) = with(binding) {
        captureFeedbackBtn.setText(titleRes)
        captureFeedbackTxtExplanation.setText(explanationRes)
        captureFeedbackBtn.setCheckedWithLeftDrawable(false)
        setManualCaptureButtonClickable(false)
    }

    private fun renderValidating() = with(binding) {
        captureFeedbackBtn.setText(IDR.string.face_capture_title_validating)
        captureFeedbackBtn.setCheckedWithLeftDrawable(false)
        setManualCaptureButtonClickable(false)
        captureFeedbackTxtExplanation.isVisible = false
        captureFeedbackBtn.isVisible = true
        captureFeedbackPermissionButton.isGone = true
    }

    private fun renderValidationFailed() = with(binding) {
        captureFeedbackBtn.setText(IDR.string.face_capture_title_validating_failed)
        captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_validating_failed)
        captureFeedbackBtn.isVisible = true
        captureFeedbackPermissionButton.isGone = true
    }

    private fun onCaptureFinished(result: List<FaceDetection>) {
        if (finishedHandled) return
        finishedHandled = true
        mainVm.captureFinished(result)
        findNavController().navigateSafely(
            currentFragment = this,
            directions = LiveFeedbackFragmentDirections.actionFaceLiveFeedbackFragmentToFaceConfirmationFragment(),
        )
    }

    private fun FragmentLiveFeedbackBinding.setManualCaptureButtonClickable(clickable: Boolean) {
        if (!vm.isAutoCapture) {
            captureFeedbackBtn.isClickable = clickable
        }
    }

    private fun renderNoPermission() {
        binding.apply {
            renderControls(explanationVisible = true)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_permission_denied)
            captureFeedbackBtn.isGone = true
            captureFeedbackPermissionButton.isVisible = true
            setManualCaptureButtonClickable(false)
        }
    }
}

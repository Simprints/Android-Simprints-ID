package com.simprints.face.capture.screens.livefeedback

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
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
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.tools.extentions.hasCameraFlash
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.face.capture.R
import com.simprints.face.capture.databinding.FragmentLiveFeedbackBinding
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.screens.FaceCaptureViewModel
import com.simprints.infra.camera.CameraFrameProvider
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.awaitLayout
import com.simprints.infra.uibase.view.setCheckedWithLeftDrawable
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class LiveFeedbackFragment : Fragment(R.layout.fragment_live_feedback) {
    @Inject
    lateinit var cameraFrameProviderFactory: CameraFrameProvider.Factory

    private val cameraFrameProvider: CameraFrameProvider by lazy {
        cameraFrameProviderFactory.create(crashReportTag = FACE_CAPTURE)
    }
    private var overlayRect: RectF = RectF()
    private var overlayWidth: Int = 0
    private var overlayHeight: Int = 0

    private val mainVm: FaceCaptureViewModel by activityViewModels()
    private val vm: LiveFeedbackViewModel by viewModels()
    private val binding by viewBinding(FragmentLiveFeedbackBinding::bind)

    private var cameraStarted = false
    private var permissionStatus: PermissionStatus = PermissionStatus.Granted
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
        permissionStatus = requireActivity().permissionFromResult(Manifest.permission.CAMERA, granted)
        if (permissionStatus == PermissionStatus.Granted) setUpCamera() else renderNoPermission()
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
        binding.captureProgress.max = 1 // normalized progress

        // `isAutoCapture` affects major parts of the UI state, so resolve it before wiring the capture button
        viewLifecycleOwner.lifecycleScope.launch {
            vm.initAutoCapture()
            setUpCaptureButton()
        }

        // Wait till the views gets its final size then init frame processor and setup the camera
        binding.faceCaptureCamera.post {
            if (view != null) {
                vm.initCapture(mainVm.bioSDK, mainVm.samplesToCapture, mainVm.attemptNumber)
            }
        }

        binding.captureInstructionsBtn.setOnClickListener {
            findNavController().navigateSafely(
                currentFragment = this,
                directions = LiveFeedbackFragmentDirections.actionFaceLiveFeedbackFragmentToFacePreparationFragment(),
            )
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

    private fun setUpCamera() = viewLifecycleOwner.lifecycleScope.launch {
        if (cameraStarted) return@launch
        cameraStarted = true

        binding.captureOverlay.awaitLayout()

        overlayRect = RectF(binding.captureOverlay.circleRect)
        overlayWidth = binding.captureOverlay.width
        overlayHeight = binding.captureOverlay.height

        cameraFrameProvider.bind(viewLifecycleOwner, binding.faceCaptureCamera)

        launch(Dispatchers.Default) {
            vm.detectorReady.first { it }
            cameraFrameProvider.frames.collect { rawBitmap ->
                analyze(rawBitmap)
            }
        }

        Simber.i("Camera setup finished", tag = FACE_CAPTURE)
    }

    override fun onResume() {
        super.onResume()
        when {
            requireActivity().hasPermission(Manifest.permission.CAMERA) -> {
                setUpCamera()
                toggleCaptureButtonIfAutoCapture(true)
            }

            mainVm.shouldCheckCameraPermissions.getAndSet(false) -> {
                // Check permission in onResume() so that if user left the app to go to Settings
                // and give the permission, it's reflected when they come back to SID
                if (requireActivity().hasPermission(Manifest.permission.CAMERA)) {
                    setUpCamera()
                } else {
                    permissionStatus = PermissionStatus.Denied
                    launchPermissionRequest.launch(Manifest.permission.CAMERA)
                }
            }

            else -> mainVm.shouldCheckCameraPermissions.set(true)
        }
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
        cameraStarted = false
        super.onDestroyView()
    }

    private fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect(::render)
            }
        }
    }

    private fun analyze(rawBitmap: Bitmap) {
        try {
            vm.process(
                rawBitmap = rawBitmap,
                overlayRect = overlayRect,
                overlayWidth = overlayWidth,
                overlayHeight = overlayHeight,
            )
        } catch (t: Throwable) {
            Simber.e("Image analysis crashed", t, tag = FACE_CAPTURE)
            lifecycleScope.launch {
                mainVm.submitError(t)
            }
        }
    }

    private fun render(state: LiveFeedbackState) {
        if (permissionStatus != PermissionStatus.Granted) return

        renderProgress(state.progress)
        when (state.phase) {
            LiveFeedbackState.Phase.NOT_STARTED -> {
                renderOverlay(overlayWhite = false, explanationVisible = false)
                renderFeedbackOnButton(state)
            }
            LiveFeedbackState.Phase.CAPTURING -> {
                renderOverlay(overlayWhite = true, explanationVisible = true)
                renderFeedbackOnButton(state)
            }
            LiveFeedbackState.Phase.VALIDATING -> {
                renderOverlay(overlayWhite = true, explanationVisible = true)
                renderValidating()
            }
            LiveFeedbackState.Phase.VALIDATION_FAILED -> {
                renderOverlay(overlayWhite = true, explanationVisible = true)
                renderValidationFailed()
            }
            LiveFeedbackState.Phase.FINISHED -> onCaptureFinished(state.result)
        }
    }

    private fun renderProgress(progress: Progress) = with(binding.captureProgress) {
        value = progress.value
        progressColor = when (progress.tint) {
            Progress.Tint.DEFAULT -> defaultCaptureProgressColor
            Progress.Tint.VALID -> validCaptureProgressColor
            Progress.Tint.VALIDATION -> validationProgressColor
        }
        isInvisible = !progress.visible
    }

    private fun renderOverlay(
        overlayWhite: Boolean,
        explanationVisible: Boolean,
    ) = with(binding) {
        if (overlayWhite) {
            captureOverlay.drawWhiteTarget()
            captureFeedbackTxtExplanation.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey))
        } else {
            captureOverlay.drawSemiTransparentTarget()
            captureFeedbackTxtExplanation.setTextColor(ContextCompat.getColor(requireContext(), IDR.color.simprints_text_white))
        }

        captureFeedbackTxtExplanation.isVisible = explanationVisible
        captureFeedbackBtn.isVisible = true
        captureFeedbackPermissionButton.isGone = true
    }

    private fun renderFeedbackOnButton(state: LiveFeedbackState) = with(binding) {
        val feedback = if (state.isAutoCapture && state.phase != LiveFeedbackState.Phase.CAPTURING) {
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
        captureOverlay.drawWhiteTarget()
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
            renderOverlay(overlayWhite = false, explanationVisible = true)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_permission_denied)
            captureFeedbackBtn.isGone = true
            captureFeedbackPermissionButton.isVisible = true
            captureFeedbackPermissionButton.setOnClickListener {
                if (permissionStatus == PermissionStatus.DeniedNeverAskAgain) {
                    requireActivity().startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            "package:${requireActivity().packageName}".toUri(),
                        ),
                    )
                } else {
                    launchPermissionRequest.launch(Manifest.permission.CAMERA)
                }
            }
            setManualCaptureButtonClickable(false)
        }
    }
}

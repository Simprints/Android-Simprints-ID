package com.simprints.face.capture.screens.livefeedback

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Size
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.face.capture.R
import com.simprints.face.capture.databinding.FragmentLiveFeedbackBinding
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.screens.FaceCaptureViewModel
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.setCheckedWithLeftDrawable
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    @Inject
    lateinit var playFaceCaptureSounds: PlayFaceCaptureSoundsUseCase

    private val mainVm: FaceCaptureViewModel by activityViewModels()

    private val vm: LiveFeedbackFragmentViewModel by viewModels()
    private val binding by viewBinding(FragmentLiveFeedbackBinding::bind)

    private lateinit var screenSize: Size
    private lateinit var targetResolution: Size

    private val launchPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        when (requireActivity().permissionFromResult(Manifest.permission.CAMERA, granted)) {
            PermissionStatus.Granted -> setUpCamera()
            PermissionStatus.Denied -> renderNoPermission(false)
            PermissionStatus.DeniedNeverAskAgain -> renderNoPermission(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragment()
    }

    private fun initFragment() {
        screenSize = with(resources.displayMetrics) { Size(widthPixels, widthPixels) }
        bindViewModel()

        binding.captureFeedbackBtn.setOnClickListener { vm.startCapture() }
        binding.captureProgress.max = mainVm.samplesToCapture

        //Wait till the views gets its final size then init frame processor and setup the camera
        binding.faceCaptureCamera.post {
            if (view != null) {
                vm.initCapture(mainVm.samplesToCapture, mainVm.attemptNumber)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (vm.capturingState.value == LiveFeedbackFragmentViewModel.CapturingState.CAPTURING) {
                vm.cancelCapture()
            } else {
                mainVm.handleBackButton()
            }
        }
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() = lifecycleScope.launch {
        if (::cameraExecutor.isInitialized && !cameraExecutor.isShutdown) {
            return@launch
        }
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        // ImageAnalysis
        //Todo choose accurate output image resolution that respects quality,performance and face analysis SDKs https://simprints.atlassian.net/browse/CORE-2569
        if (!::targetResolution.isInitialized) {
            targetResolution = Size(binding.captureOverlay.width, binding.captureOverlay.height)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetResolution(targetResolution)
            .setOutputImageRotationEnabled(true)
            .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
        val cropAnalyzer = CropToTargetOverlayAnalyzer(binding.captureOverlay, ::analyze)

        imageAnalyzer.setAnalyzer(cameraExecutor, cropAnalyzer)

        // Preview
        val preview = Preview.Builder().setTargetResolution(targetResolution).build()
        val cameraProvider = ProcessCameraProvider.awaitInstance(requireContext())
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            this@LiveFeedbackFragment, DEFAULT_BACK_CAMERA, preview, imageAnalyzer
        )
        // Attach the view's surface provider to preview use case
        preview.surfaceProvider = binding.faceCaptureCamera.surfaceProvider
    }

    override fun onResume() {
        super.onResume()

        when {
            requireActivity().hasPermission(Manifest.permission.CAMERA) -> setUpCamera()
            mainVm.shouldCheckCameraPermissions.getAndSet(false) -> {
                // Check permission in onResume() so that if user left the app to go to Settings
                // and give the permission, it's reflected when they come back to SID
                if (requireActivity().hasPermission(Manifest.permission.CAMERA)) {
                    setUpCamera()
                } else {
                    launchPermissionRequest.launch(Manifest.permission.CAMERA)
                }
            }

            else -> mainVm.shouldCheckCameraPermissions.set(true)
        }
    }

    override fun onStop() {
        // Shut down our background executor
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        super.onStop()
    }

    private fun bindViewModel() {
        vm.currentDetection.observe(viewLifecycleOwner) {
            renderCurrentDetection(it)
        }

        vm.capturingState.observe(viewLifecycleOwner) {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA") when (it) {
                LiveFeedbackFragmentViewModel.CapturingState.PREPARING -> renderPreparing()

                LiveFeedbackFragmentViewModel.CapturingState.READY_TO_CAPTURE ->  renderReadyToCapture()

                LiveFeedbackFragmentViewModel.CapturingState.CAPTURING -> renderCapturing()

                LiveFeedbackFragmentViewModel.CapturingState.PROCESSING -> renderProcessing()

                LiveFeedbackFragmentViewModel.CapturingState.FINISHED -> {
                    playFaceCaptureSounds.stopSound()
                    mainVm.captureFinished(vm.sortedQualifyingCaptures)
                    findNavController().navigateSafely(
                        currentFragment = this,
                        actionId = R.id.action_faceLiveFeedbackFragment_to_faceConfirmationFragment
                    )
                }

            }
        }
    }

    private fun analyze(image: Bitmap) {
        try {
            vm.process(croppedBitmap = image)
        } catch (t: Throwable) {
            Simber.e(t)
            // Image analysis is running in bg thread
            lifecycleScope.launch {
                mainVm.submitError(t)
            }
        }
    }

    private fun renderCurrentDetection(faceDetection: FaceDetection) {
        if (vm.capturingState.value != LiveFeedbackFragmentViewModel.CapturingState.CAPTURING) {
            return
        }

        when (faceDetection.status) {
            FaceDetection.Status.NOFACE -> renderNoFace()
            FaceDetection.Status.OFFYAW -> renderFaceNotStraight()
            FaceDetection.Status.OFFROLL -> renderFaceNotStraight()
            FaceDetection.Status.TOOCLOSE -> renderFaceTooClose()
            FaceDetection.Status.TOOFAR -> renderFaceTooFar()
            FaceDetection.Status.BAD_QUALITY -> renderBadQuality()
            FaceDetection.Status.VALID -> renderValidFace()
            FaceDetection.Status.VALID_CAPTURING -> renderValidCapturingFace()
        }
    }

    private fun renderCapturingStateColors() {
        with(binding) {
            captureOverlay.drawWhiteTarget()
            captureFeedbackTxtExplanation.setTextColor(
                ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey_light)
            )
        }
    }

    private fun renderPreparing() {
        binding.apply {
            captureOverlay.drawSemiTransparentTarget()
            captureFeedbackBtn.setText(IDR.string.face_capture_title_previewing)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true
        }
        setCaptureButtonClickable(false)
    }

    private fun renderReadyToCapture() {
        playFaceCaptureSounds.stopSound()
        binding.apply {
            captureOverlay.drawSemiTransparentTarget()
            captureFeedbackBtn.setText(IDR.string.face_capture_begin_button)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(
                true, ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp)
            )
        }
        setCaptureButtonClickable(true)
        renderProgressBar(true)
    }

    private fun renderCapturing() {
        renderCapturingStateColors()
        playFaceCaptureSounds.playAttentionSound()
        binding.apply {
            captureProgress.isVisible = true
            captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true
        }
        setCaptureButtonClickable(false)
    }

    private fun renderProcessing() {
        playFaceCaptureSounds.playCameraShutterSound()
        binding.apply {
            captureProgress.isVisible = true
            captureFeedbackBtn.setText(IDR.string.face_capture_button_processing)
            captureFeedbackBtn.isVisible = true
            captureFeedbackTxtExplanation.text = null
            captureFeedbackPermissionButton.isGone = true
        }
        setCaptureButtonClickable(false)
    }

    private fun renderValidFace() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_begin_button)
            captureFeedbackTxtExplanation.text = null
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(
                true, ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp)
            )
        }
        setCaptureButtonClickable(true)
    }

    private fun renderValidCapturingFace() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_hold)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(
                true, ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp)
            )
        }

        renderProgressBar(true)
    }

    private fun renderFaceTooFar() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_too_far)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_too_far)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
        }
    }

    private fun renderFaceTooClose() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_too_close)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_too_close)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
        }
    }

    private fun renderNoFace() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_no_face)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_no_face)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
        }
    }

    private fun renderFaceNotStraight() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_look_straight)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_look_straight)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
        }
    }

    private fun renderBadQuality() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_bad_quality)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_bad_quality)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
        }
    }

    private fun renderProgressBar(valid: Boolean) {
        binding.apply {
            val progressColor = if (valid) IDR.color.simprints_green_light
            else IDR.color.simprints_blue_grey_light

            captureProgress.progressColor = ContextCompat.getColor(
                requireContext(), progressColor
            )

            captureProgress.value = vm.userCaptures.size.toFloat()
        }
    }

    private fun setCaptureButtonClickable(valid: Boolean) {
        binding.captureFeedbackBtn.isClickable = valid
    }

    private fun renderNoPermission(shouldOpenSettings: Boolean) {
        binding.apply {
            captureOverlay.drawSemiTransparentTarget()
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_permission_denied)
            captureFeedbackBtn.isGone = true
            captureFeedbackPermissionButton.isVisible = true
            captureFeedbackPermissionButton.setOnClickListener {
                if (shouldOpenSettings) {
                    requireActivity().startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${requireActivity().packageName}")
                        )
                    )
                } else {
                    launchPermissionRequest.launch(Manifest.permission.CAMERA)
                }
            }
        }
        setCaptureButtonClickable(false)
    }

}



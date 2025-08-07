package com.simprints.face.capture.screens.livefeedback

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.Settings
import android.util.Size
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.tools.extentions.hasCameraFlash
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.face.capture.R
import com.simprints.face.capture.databinding.FragmentLiveFeedbackBinding
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.screens.FaceCaptureViewModel
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.setCheckedWithLeftDrawable
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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

    private val mainVm: FaceCaptureViewModel by activityViewModels()

    private val vm: LiveFeedbackFragmentViewModel by viewModels()
    private val binding by viewBinding(FragmentLiveFeedbackBinding::bind)

    private lateinit var screenSize: Size
    private lateinit var targetResolution: Size

    private var cameraControl: CameraControl? = null

    private val validCaptureProgressColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_green_light)
    private val defaultCaptureProgressColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey_light)

    private val launchPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        when (requireActivity().permissionFromResult(Manifest.permission.CAMERA, granted)) {
            PermissionStatus.Granted -> setUpCamera()
            PermissionStatus.Denied -> renderNoPermission(false)
            PermissionStatus.DeniedNeverAskAgain -> renderNoPermission(true)
        }
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
        screenSize = with(resources.displayMetrics) { Size(widthPixels, widthPixels) }
        bindViewModel()
        binding.captureProgress.max = 1 // normalized progress

        binding.captureFeedbackBtn.setOnClickListener {
            vm.startCapture()
            if (vm.isAutoCapture) {
                binding.captureFeedbackBtn.isClickable = false
            }
        }

        // Wait till the views gets its final size then init frame processor and setup the camera
        binding.faceCaptureCamera.post {
            if (view != null) {
                vm.initCapture(mainVm.bioSDK, mainVm.samplesToCapture, mainVm.attemptNumber)
            }
        }
        if (vm.isAutoCapture) {
            // Await until capture button is pressed
            vm.holdOffAutoCapture()
            binding.captureFeedbackBtn.isClickable = true
        }

        binding.captureInstructionsBtn.setOnClickListener {
            findNavController().navigateSafely(
                currentFragment = this,
                directions = LiveFeedbackFragmentDirections.actionFaceLiveFeedbackFragmentToFacePreparationFragment(),
            )
        }

        with(binding.captureFlashButton) {
            isSelected = false
            setOnClickListener {
                val torchEnabled = !binding.captureFlashButton.isSelected
                toggleTorche(torchEnabled)
            }
        }
    }

    private fun toggleTorche(enabled: Boolean) {
        cameraControl?.enableTorch(enabled)
        binding.captureFlashButton.isSelected = enabled
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() = lifecycleScope.launch {
        if (::cameraExecutor.isInitialized && !cameraExecutor.isShutdown) {
            return@launch
        }
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        // ImageAnalysis
        // Todo choose accurate output image resolution that respects quality,performance and face analysis SDKs https://simprints.atlassian.net/browse/CORE-2569
        if (!::targetResolution.isInitialized) {
            targetResolution = Size(binding.captureOverlay.width, binding.captureOverlay.height)
        }

        val imageAnalyzer = ImageAnalysis
            .Builder()
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
        val camera = cameraProvider.bindToLifecycle(
            this@LiveFeedbackFragment,
            DEFAULT_BACK_CAMERA,
            preview,
            imageAnalyzer,
        )
        cameraControl = camera.cameraControl
        // Attach the view's surface provider to preview use case
        preview.surfaceProvider = binding.faceCaptureCamera.surfaceProvider
        Simber.i("Camera setup finished", tag = FACE_CAPTURE)
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
        toggleTorche(false)
        // Shut down our background executor
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        super.onStop()
    }

    private fun bindViewModel() {
        vm.displayCameraFlashControls.observe(viewLifecycleOwner) {
            binding.captureFlashButton.isVisible = it && requireContext().hasCameraFlash
        }

        vm.currentDetection.observe(viewLifecycleOwner) {
            renderCurrentDetection(it)
        }

        vm.capturingState.observe(viewLifecycleOwner) {
            when (it) {
                LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED -> renderCaptureNotStarted()
                LiveFeedbackFragmentViewModel.CapturingState.CAPTURING -> renderCapturing()
                LiveFeedbackFragmentViewModel.CapturingState.FINISHED -> {
                    mainVm.captureFinished(vm.sortedQualifyingCaptures)
                    findNavController().navigateSafely(
                        currentFragment = this,
                        directions = LiveFeedbackFragmentDirections.actionFaceLiveFeedbackFragmentToFaceConfirmationFragment(),
                    )
                }
            }
        }
    }

    private fun analyze(image: Bitmap) {
        try {
            vm.process(croppedBitmap = image)
        } catch (t: Throwable) {
            Simber.e("Image analysis crashed", t, tag = FACE_CAPTURE)
            // Image analysis is running in bg thread
            lifecycleScope.launch {
                mainVm.submitError(t)
            }
        }
    }

    private fun renderCurrentDetection(faceDetection: FaceDetection) {
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

    private fun renderCaptureNotStarted() {
        binding.apply {
            if (vm.isAutoCapture) {
                captureFeedbackBtn.setText(IDR.string.face_capture_start_capture)
                captureFeedbackBtn.isChecked = true
            } else {
                captureFeedbackBtn.setText(IDR.string.face_capture_title_previewing)
            }

            captureOverlay.drawSemiTransparentTarget()
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true
            setManualCaptureButtonClickable(false)
        }
    }

    private fun renderCapturing() {
        binding.apply {
            captureOverlay.drawWhiteTarget()
            captureFeedbackTxtExplanation.setTextColor(
                ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey),
            )

            captureProgress.isVisible = true
            captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true
            setManualCaptureButtonClickable(false)
        }
    }

    private fun renderValidFace() {
        binding.apply {
            if (vm.isAutoCapture) {
                captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
            } else {
                captureFeedbackBtn.setText(IDR.string.face_capture_begin_button)
            }

            captureFeedbackTxtExplanation.text = null
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(
                true,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp),
            )
            setManualCaptureButtonClickable(false)
        }
    }

    private fun renderValidCapturingFace() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_hold)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(
                true,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp),
            )
            renderProgressBar(false)
        }
    }

    private fun renderFaceTooFar() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_too_far)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_too_far)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
            setManualCaptureButtonClickable(false)
            renderProgressBar(false)
        }
    }

    private fun renderFaceTooClose() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_too_close)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_too_close)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
            setManualCaptureButtonClickable(false)
            renderProgressBar(false)
        }
    }

    private fun renderNoFace() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_no_face)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_no_face)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
            setManualCaptureButtonClickable(false)
            renderProgressBar(false)
        }
    }

    private fun renderFaceNotStraight() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_look_straight)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_look_straight)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
            setManualCaptureButtonClickable(false)
            renderProgressBar(false)
        }
    }

    private fun renderBadQuality() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_title_bad_quality)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_error_bad_quality)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
            setManualCaptureButtonClickable(false)
            renderProgressBar(false)
        }
    }

    private fun FragmentLiveFeedbackBinding.renderProgressBar(validCapture: Boolean) {
        captureProgress.progressColor = if (validCapture) validCaptureProgressColor else defaultCaptureProgressColor
        captureProgress.value = vm.getNormalizedProgress()
    }

    private fun FragmentLiveFeedbackBinding.setManualCaptureButtonClickable(clickable: Boolean) {
        if (!vm.isAutoCapture) {
            captureFeedbackBtn.isClickable = clickable
        }
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

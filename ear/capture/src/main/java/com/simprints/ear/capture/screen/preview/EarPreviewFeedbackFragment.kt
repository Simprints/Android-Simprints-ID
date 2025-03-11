package com.simprints.ear.capture.screen.preview

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Size
import android.view.View
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
import com.simprints.ear.capture.R
import com.simprints.ear.capture.databinding.FragmentEarPreviewBinding
import com.simprints.ear.capture.models.EarDetection
import com.simprints.ear.capture.screen.EarCaptureViewModel
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.setCheckedWithLeftDrawable
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class EarPreviewFeedbackFragment : Fragment(R.layout.fragment_ear_preview) {
    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    private val mainVm: EarCaptureViewModel by activityViewModels()

    private val vm: EarPreviewFragmentViewModel by viewModels()
    private val binding by viewBinding(FragmentEarPreviewBinding::bind)

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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("LiveFeedbackFragment started", tag = ORCHESTRATION)
        initFragment()
    }

    private fun initFragment() {
        screenSize = with(resources.displayMetrics) { Size(widthPixels, widthPixels) }
        bindViewModel()

        binding.captureFeedbackBtn.setOnClickListener { vm.startCapture() }
        binding.captureProgress.max = mainVm.samplesToCapture

        // Wait till the views gets its final size then init frame processor and setup the camera
        binding.faceCaptureCamera.post {
            if (view != null) {
                vm.initCapture(mainVm.samplesToCapture, mainVm.attemptNumber)
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
        cameraProvider.bindToLifecycle(
            this@EarPreviewFeedbackFragment,
            DEFAULT_BACK_CAMERA,
            preview,
            imageAnalyzer,
        )
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
                EarPreviewFragmentViewModel.CapturingState.NOT_STARTED -> renderCapturingNotStarted()

                EarPreviewFragmentViewModel.CapturingState.CAPTURING -> renderCapturing()

                EarPreviewFragmentViewModel.CapturingState.FINISHED -> {
                    mainVm.captureFinished(vm.sortedQualifyingCaptures)
                    findNavController().navigateSafely(
                        currentFragment = this,
                        actionId = R.id.action_earPreviewFeedbackFragment_to_earConfirmationFragment,
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

    private fun renderCurrentDetection(faceDetection: EarDetection) {
        when (faceDetection.status) {
            EarDetection.Status.NO_EAR -> renderNoEar()
            EarDetection.Status.VALID -> renderValidFace()
            EarDetection.Status.VALID_CAPTURING -> renderValidCapturingFace()
        }
    }

    private fun renderCapturingStateColors() {
        with(binding) {
            captureOverlay.drawWhiteTarget()
            captureFeedbackTxtExplanation.setTextColor(
                ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey),
            )
        }
    }

    private fun renderCapturingNotStarted() {
        binding.apply {
            captureOverlay.drawSemiTransparentTarget()
            captureFeedbackBtn.setText(IDR.string.face_capture_title_previewing)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true
        }
        toggleCaptureButtons(false)
    }

    private fun renderCapturing() {
        renderCapturingStateColors()
        binding.apply {
            captureProgress.isVisible = true
            captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true
        }
        toggleCaptureButtons(false)
    }

    private fun renderValidFace() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_begin_button)
            captureFeedbackTxtExplanation.text = null
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(
                true,
                ContextCompat.getDrawable(requireContext(), IDR.drawable.ic_checked_white_18dp),
            )
        }
        toggleCaptureButtons(true)
    }

    private fun renderValidCapturingFace() {
        binding.apply {
            captureFeedbackBtn.setText(IDR.string.face_capture_prep_begin_button_capturing)
            captureFeedbackTxtExplanation.setText(IDR.string.face_capture_hold)
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(
                true,
                ContextCompat.getDrawable(requireContext(), IDR.drawable.ic_checked_white_18dp),
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

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderNoEar() {
        binding.apply {
            captureFeedbackBtn.setText("No ear")
            captureFeedbackTxtExplanation.setText("Show more ear")
            captureFeedbackBtn.isVisible = true
            captureFeedbackPermissionButton.isGone = true

            captureFeedbackBtn.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderProgressBar(valid: Boolean) {
        binding.apply {
            val progressColor = if (valid) {
                IDR.color.simprints_green_light
            } else {
                IDR.color.simprints_blue_grey_light
            }

            captureProgress.progressColor = ContextCompat.getColor(
                requireContext(),
                progressColor,
            )

            captureProgress.value = vm.userCaptures.size.toFloat()
        }
    }

    private fun toggleCaptureButtons(valid: Boolean) {
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
                            Uri.parse("package:${requireActivity().packageName}"),
                        ),
                    )
                } else {
                    launchPermissionRequest.launch(Manifest.permission.CAMERA)
                }
            }
        }
        toggleCaptureButtons(false)
    }
}

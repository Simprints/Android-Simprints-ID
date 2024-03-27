package com.simprints.face.capture.screens.livefeedback

import android.Manifest
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.await
import com.simprints.core.tools.extentions.hasPermission
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
import com.simprints.infra.resources.R as IDR


/**
 * This is the class presented as the user is capturing theface, they are presented with this fragment, which displays
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


    private val launchPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            Simber.i("Camera Permission not granted")
            Toast.makeText(
                requireContext(),
                IDR.string.face_capturing_permission_denied,
                Toast.LENGTH_LONG
            ).show()
        }
        // init fragment anyway
        initFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireActivity().hasPermission(Manifest.permission.CAMERA)) {
            initFragment()
        } else {
            launchPermissionRequest.launch(Manifest.permission.CAMERA)
        }
    }

    private fun initFragment() {
        screenSize = with(resources.displayMetrics) { Size(widthPixels, widthPixels) }

        bindViewModel()

        binding.captureFeedbackTxtTitle.setOnClickListener { vm.startCapture() }
        binding.captureProgress.max = mainVm.samplesToCapture

        //Wait till the views gets its final size then init frame processor and setup the camera
        binding.faceCaptureCamera.post {
            if (view != null) {
                vm.initFrameProcessor(
                    mainVm.samplesToCapture, mainVm.attemptNumber,
                    binding.captureOverlay.rectInCanvas,
                    Size(binding.captureOverlay.width, binding.captureOverlay.height),
                )
                setUpCamera()
            }
        }
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() = lifecycleScope.launch {
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        // ImageAnalysis
        //Todo choose accurate output image resolution that respects quality,performance and face analysis SDKs https://simprints.atlassian.net/browse/CORE-2569
        val targetResolution = Size(binding.captureOverlay.width, binding.captureOverlay.height)
        val imageAnalyzer = ImageAnalysis.Builder().setTargetResolution(targetResolution)
            .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
        imageAnalyzer.setAnalyzer(cameraExecutor, ::analyze)
        // Preview
        val preview = Preview.Builder().setTargetResolution(targetResolution).build()
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
        cameraProvider.bindToLifecycle(
            this@LiveFeedbackFragment, DEFAULT_BACK_CAMERA, preview, imageAnalyzer
        )
        // Attach the view's surface provider to preview use case
        preview.setSurfaceProvider(binding.faceCaptureCamera.surfaceProvider)
    }

    override fun onStop() {
        // Shut down our background executor
        if(::cameraExecutor.isInitialized) {
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
                LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED -> renderCapturingNotStarted()

                LiveFeedbackFragmentViewModel.CapturingState.CAPTURING -> renderCapturing()

                LiveFeedbackFragmentViewModel.CapturingState.FINISHED -> {
                    mainVm.captureFinished(vm.sortedQualifyingCaptures)
                    findNavController().navigateSafely(this, R.id.action_faceLiveFeedbackFragment_to_faceConfirmationFragment)
                }

            }
        }
    }

    private fun analyze(image: ImageProxy) {
        try {
            vm.process(image)
        } catch (t: Throwable) {
            Simber.e(t)
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
            FaceDetection.Status.VALID -> renderValidFace()
            FaceDetection.Status.VALID_CAPTURING -> renderValidCapturingFace()
        }
    }

    private fun renderCapturingStateColors() {
        with(binding) {
            captureOverlay.drawWhiteTarget()

            captureTitle.setTextColor(
                ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey)
            )
            captureFeedbackTxtExplanation.setTextColor(
                ContextCompat.getColor(requireContext(), IDR.color.simprints_blue_grey)
            )
        }
    }

    private fun renderCapturingNotStarted() {
        binding.apply {
            captureOverlay.drawSemiTransparentTarget()
            captureTitle.text = getString(IDR.string.face_capture_preparation_title)
            captureFeedbackTxtTitle.text = getString(IDR.string.face_capture_title_previewing)
        }
        toggleCaptureButtons(false)
    }

    private fun renderCapturing() {
        renderCapturingStateColors()
        binding.apply {
            captureProgress.isVisible = true
            captureTitle.text = getString(IDR.string.face_capture_capturing_title)
            captureFeedbackTxtTitle.text =
                getString(IDR.string.face_capture_prep_begin_button_capturing)
        }
        toggleCaptureButtons(false)
    }

    private fun renderValidFace() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(IDR.string.face_capture_begin_button)
            captureFeedbackTxtExplanation.text = null

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(
                true, ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp)
            )
        }
        toggleCaptureButtons(true)
    }

    private fun renderValidCapturingFace() {
        binding.apply {
            captureFeedbackTxtTitle.text =
                getString(IDR.string.face_capture_prep_begin_button_capturing)
            captureFeedbackTxtExplanation.text = getString(IDR.string.face_capture_hold)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(
                true, ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp)
            )
        }

        renderProgressBar(true)
    }

    private fun renderFaceTooFar() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(IDR.string.face_capture_title_too_far)
            captureFeedbackTxtExplanation.text = getString(IDR.string.face_capture_error_too_far)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderFaceTooClose() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(IDR.string.face_capture_title_too_close)
            captureFeedbackTxtExplanation.text = getString(IDR.string.face_capture_error_too_close)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderNoFace() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(IDR.string.face_capture_title_no_face)
            captureFeedbackTxtExplanation.text = getString(IDR.string.face_capture_error_no_face)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderFaceNotStraight() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(IDR.string.face_capture_title_look_straight)
            captureFeedbackTxtExplanation.text =
                getString(IDR.string.face_capture_error_look_straight)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
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

    private fun toggleCaptureButtons(valid: Boolean) {
        binding.captureFeedbackTxtTitle.isClickable = valid
    }
}



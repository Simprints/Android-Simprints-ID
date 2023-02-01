package com.simprints.face.capture.livefeedback

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import com.simprints.core.tools.extentions.setCheckedWithLeftDrawable
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.databinding.FragmentLiveFeedbackBinding
import com.simprints.face.models.FaceDetection
import com.simprints.face.models.Size
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is the class presented as the user is capturing theface, they are presented with this fragment, which displays
 * live information about distance and whether the face is ready to be captured or not.
 * It also displays the capture process of the face and then sends this result to
 * [com.simprints.face.capture.confirmation.ConfirmationFragment]
 */
@AndroidEntryPoint
class LiveFeedbackFragment : Fragment(R.layout.fragment_live_feedback), FrameProcessor {
    private val mainVm: FaceCaptureViewModel by activityViewModels()
    private val vm: LiveFeedbackFragmentViewModel by viewModels()
    private val binding by viewBinding(FragmentLiveFeedbackBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.faceCaptureCamera.setLifecycleOwner(viewLifecycleOwner)
        bindViewModel()
        binding.captureFeedbackTxtTitle.setOnClickListener { vm.startCapture() }
        binding.captureProgress.max = mainVm.samplesToCapture
    }

    override fun onResume() {
        binding.faceCaptureCamera.addFrameProcessor(this)
        super.onResume()
    }

    override fun onStop() {
        binding.faceCaptureCamera.removeFrameProcessor(this)
        super.onStop()
    }

    private fun bindViewModel() {
        vm.currentDetection.observe(viewLifecycleOwner) {
            renderCurrentDetection(it)
//            renderDebugInfo(it.face)
        }

        vm.capturingState.observe(viewLifecycleOwner) {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (it) {
                LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED ->
                    renderCapturingNotStarted()
                LiveFeedbackFragmentViewModel.CapturingState.CAPTURING ->
                    renderCapturing()
                LiveFeedbackFragmentViewModel.CapturingState.FINISHED -> {
                    mainVm.captureFinished(vm.sortedQualifyingCaptures)
                    findNavController().navigate(R.id.action_liveFeedbackFragment_to_confirmationFragment)
                }

            }
        }
    }

    /**
     * This method  needs to block because frame is a singleton which cannot be released until it's
     * converted into a preview frame. Although it's blocking, this is running in a background thread.
     * https://natario1.github.io/CameraView/docs/frame-processing
     *
     * Also the frame sometimes throws IllegalStateException for null width and height
     */
    override fun process(frame: Frame) {
        try {
            vm.process(
                frame,
                binding.captureOverlay.rectInCanvas,
                Size(binding.captureOverlay.width, binding.captureOverlay.height),
                mainVm.samplesToCapture,
                mainVm.attemptNumber
            )

        } catch (t: Throwable) {
            Simber.e(t)
            mainVm.submitError(t)
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
                ContextCompat.getColor(requireContext(), R.color.capture_grey_blue)
            )
            captureFeedbackTxtExplanation.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.capture_grey_blue)
            )
        }
    }

    private fun renderCapturingNotStarted() {
        binding.apply {
            captureOverlay.drawSemiTransparentTarget()
            captureTitle.text = getString(R.string.title_preparation)
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_previewing)
        }
        toggleCaptureButtons(false)
    }

    private fun renderCapturing() {
        renderCapturingStateColors()
        binding.apply {
            captureProgress.isVisible = true
            captureTitle.text = getString(R.string.title_capturing)
            captureFeedbackTxtTitle.text = getString(R.string.capture_prep_begin_btn_capturing)
        }
        toggleCaptureButtons(false)
    }

    private fun renderValidFace() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_prep_begin_btn)
            captureFeedbackTxtExplanation.text = null

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(
                true,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp)
            )
        }
        toggleCaptureButtons(true)
    }

    private fun renderValidCapturingFace() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_prep_begin_btn_capturing)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_hold)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(
                true,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_checked_white_18dp)
            )
        }

        renderProgressBar(true)
    }

    private fun renderFaceTooFar() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_face_too_far)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_error_face_too_far)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderFaceTooClose() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_too_close)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_error_face_too_close)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderNoFace() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_no_face)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_error_no_face)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderFaceNotStraight() {
        binding.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_look_straight)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_error_look_straight)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderProgressBar(valid: Boolean) {
        binding.apply {
            val progressColor =
                if (valid) R.color.capture_green
                else R.color.capture_grey

            captureProgress.progressColor = ContextCompat.getColor(
                requireContext(),
                progressColor
            )

            captureProgress.value = vm.userCaptures.size.toFloat()
        }
    }

    private fun toggleCaptureButtons(valid: Boolean) {
        binding.captureFeedbackTxtTitle.isClickable = valid
    }
}



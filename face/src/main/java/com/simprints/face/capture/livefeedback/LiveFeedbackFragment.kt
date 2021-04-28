package com.simprints.face.capture.livefeedback

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import com.simprints.core.tools.extentions.setCheckedWithLeftDrawable
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.detection.Face
import com.simprints.face.models.FaceDetection
import com.simprints.uicomponents.models.Size
import kotlinx.android.synthetic.main.fragment_live_feedback.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import com.simprints.uicomponents.R as UCR

class LiveFeedbackFragment : Fragment(R.layout.fragment_live_feedback), FrameProcessor {
    private val mainVm: FaceCaptureViewModel by sharedViewModel()
    private val vm: LiveFeedbackFragmentViewModel by viewModel { parametersOf(mainVm) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        face_capture_camera.setLifecycleOwner(viewLifecycleOwner)
        bindViewModel()
        capture_feedback_txt_title.setOnClickListener { vm.startCapture() }
        capture_progress.max = mainVm.samplesToCapture
    }

    override fun onResume() {
        face_capture_camera.addFrameProcessor(this)
        super.onResume()
    }

    override fun onStop() {
        face_capture_camera.removeFrameProcessor(this)
        super.onStop()
    }

    private fun bindViewModel() {
        vm.currentDetection.observe(viewLifecycleOwner, Observer {
            renderCurrentDetection(it)
//            renderDebugInfo(it.face)
        })

        vm.capturingState.observe(viewLifecycleOwner, Observer {
            when (it) {
                LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED ->
                    renderCapturingNotStarted()
                LiveFeedbackFragmentViewModel.CapturingState.CAPTURING ->
                    renderCapturing()
                LiveFeedbackFragmentViewModel.CapturingState.FINISHED ->
                    findNavController().navigate(R.id.action_liveFeedbackFragment_to_confirmationFragment)
                LiveFeedbackFragmentViewModel.CapturingState.FINISHED_FAILED ->
                    findNavController().navigate(R.id.action_liveFeedbackFragment_to_retryFragment)
            }
        })
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
            vm.process(frame, capture_overlay.rectInCanvas, Size(capture_overlay.width, capture_overlay.height))
        } catch (t: Throwable) {
            Timber.e(t)
            mainVm.submitError(t)
        }
    }

    private fun renderDebugInfo(face: Face?) {
        if (face == null) {
            capture_feedback_txt_debug.text = null
        } else {
            capture_feedback_txt_debug.text = getString(
                R.string.capture_debug_features,
                face.yaw.toString(),
                face.roll.toString()
            )
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
        capture_overlay.drawWhiteTarget()

        capture_title.setTextColor(
            ContextCompat.getColor(requireContext(), UCR.color.capture_grey_blue)
        )
        capture_feedback_txt_explanation.setTextColor(
            ContextCompat.getColor(requireContext(), UCR.color.capture_grey_blue)
        )
    }

    private fun renderCapturingNotStarted() {
        capture_overlay.drawSemiTransparentTarget()
        capture_title.text = getString(R.string.title_preparation)
        capture_feedback_txt_title.text = getString(R.string.capture_title_previewing)
        toggleCaptureButtons(false)
    }

    private fun renderCapturing() {
        renderCapturingStateColors()
        capture_progress.isVisible = true
        capture_title.text = getString(R.string.title_capturing)
        capture_feedback_txt_title.text =
            getString(R.string.capture_prep_begin_btn_capturing)
        toggleCaptureButtons(false)
    }

    private fun renderValidFace() {
        capture_feedback_txt_title.text = getString(R.string.capture_prep_begin_btn)
        capture_feedback_txt_explanation.text = null

        capture_feedback_txt_title.setCheckedWithLeftDrawable(
            true,
            ContextCompat.getDrawable(requireContext(), UCR.drawable.ic_checked_white_18dp)
        )
        toggleCaptureButtons(true)
    }

    private fun renderValidCapturingFace() {

        capture_feedback_txt_title.text = getString(R.string.capture_prep_begin_btn_capturing)
        capture_feedback_txt_explanation.text = getString(R.string.capture_hold)

        capture_feedback_txt_title.setCheckedWithLeftDrawable(
            true,
            ContextCompat.getDrawable(requireContext(), UCR.drawable.ic_checked_white_18dp)
        )

        renderProgressBar(true)
    }

    private fun renderFaceTooFar() {
        capture_feedback_txt_title.text = getString(R.string.capture_title_face_too_far)
        capture_feedback_txt_explanation.text = getString(R.string.capture_error_face_too_far)

        capture_feedback_txt_title.setCheckedWithLeftDrawable(false)
        toggleCaptureButtons(false)

        renderProgressBar(false)
    }

    private fun renderFaceTooClose() {
        capture_feedback_txt_title.text = getString(R.string.capture_title_too_close)
        capture_feedback_txt_explanation.text = getString(R.string.capture_error_face_too_close)

        capture_feedback_txt_title.setCheckedWithLeftDrawable(false)
        toggleCaptureButtons(false)

        renderProgressBar(false)
    }

    private fun renderNoFace() {
        capture_feedback_txt_title.text = getString(R.string.capture_title_no_face)
        capture_feedback_txt_explanation.text = getString(R.string.capture_error_no_face)

        capture_feedback_txt_title.setCheckedWithLeftDrawable(false)
        toggleCaptureButtons(false)

        renderProgressBar(false)
    }

    private fun renderFaceNotStraight() {
        capture_feedback_txt_title.text = getString(R.string.capture_title_look_straight)
        capture_feedback_txt_explanation.text = getString(R.string.capture_error_look_straight)

        capture_feedback_txt_title.setCheckedWithLeftDrawable(false)
        toggleCaptureButtons(false)

        renderProgressBar(false)
    }

    private fun renderProgressBar(valid: Boolean) {
        capture_progress.progressColor =
            ContextCompat.getColor(
                requireContext(),
                if (valid) UCR.color.capture_green else UCR.color.capture_grey
            )
        capture_progress.value = vm.userCaptures.size.toFloat()
    }

    private fun toggleCaptureButtons(valid: Boolean) {
        capture_feedback_txt_title.isClickable = valid
    }

}



package com.simprints.face.capture.livefeedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.simprints.face.databinding.FragmentLiveFeedbackBinding
import com.simprints.face.detection.Face
import com.simprints.face.models.FaceDetection
import com.simprints.uicomponents.models.Size
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import com.simprints.uicomponents.R as UCR

class LiveFeedbackFragment: Fragment(), FrameProcessor {
    private val mainVm: FaceCaptureViewModel by sharedViewModel()
    private val vm: LiveFeedbackFragmentViewModel by viewModel { parametersOf(mainVm) }
    private var binding: FragmentLiveFeedbackBinding? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLiveFeedbackBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.faceCaptureCamera?.setLifecycleOwner(viewLifecycleOwner)
        bindViewModel()
        binding?.captureFeedbackTxtTitle?.setOnClickListener { vm.startCapture() }
        binding?.captureProgress?.max = mainVm.samplesToCapture
    }

    override fun onResume() {
        binding?.faceCaptureCamera?.addFrameProcessor(this)
        super.onResume()
    }

    override fun onStop() {
        binding?.faceCaptureCamera?.removeFrameProcessor(this)
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
            binding?.let {
                vm.process(
                    frame,
                    it.captureOverlay.rectInCanvas,
                    Size(it.captureOverlay.width, it.captureOverlay.height)
                )
            }
        } catch (t: Throwable) {
            Timber.e(t)
            mainVm.submitError(t)
        }
    }

    private fun renderDebugInfo(face: Face?) {
        if (face == null) {
            binding?.captureFeedbackTxtDebug?.text = null
        } else {
            binding?.captureFeedbackTxtDebug?.text = getString(
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
        binding?.apply {
            captureOverlay.drawWhiteTarget()

            captureTitle.setTextColor(
                ContextCompat.getColor(requireContext(), UCR.color.capture_grey_blue)
            )
            captureFeedbackTxtExplanation.setTextColor(
                ContextCompat.getColor(requireContext(), UCR.color.capture_grey_blue)
            )
        }
    }

    private fun renderCapturingNotStarted() {
        binding?.apply {
            captureOverlay.drawSemiTransparentTarget()
            captureTitle.text = getString(R.string.title_preparation)
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_previewing)
            toggleCaptureButtons(false)
        }
    }

    private fun renderCapturing() {
        renderCapturingStateColors()
        binding?.apply {
            captureProgress.isVisible = true
            captureTitle.text = getString(R.string.title_capturing)
            captureFeedbackTxtTitle.text = getString(R.string.capture_prep_begin_btn_capturing)
        }
        toggleCaptureButtons(false)
    }

    private fun renderValidFace() {
        binding?.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_prep_begin_btn)
            captureFeedbackTxtExplanation.text = null

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(
                true,
                ContextCompat.getDrawable(requireContext(), UCR.drawable.ic_checked_white_18dp)
            )
        }
        toggleCaptureButtons(true)
    }

    private fun renderValidCapturingFace() {
        binding?.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_prep_begin_btn_capturing)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_hold)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(
                true,
                ContextCompat.getDrawable(requireContext(), UCR.drawable.ic_checked_white_18dp)
            )
        }

        renderProgressBar(true)
    }

    private fun renderFaceTooFar() {
        binding?.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_face_too_far)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_error_face_too_far)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderFaceTooClose() {
        binding?.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_too_close)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_error_face_too_close)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderNoFace() {
        binding?.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_no_face)
            captureFeedbackTxtTitle.text = getString(R.string.capture_error_no_face)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderFaceNotStraight() {
        binding?.apply {
            captureFeedbackTxtTitle.text = getString(R.string.capture_title_look_straight)
            captureFeedbackTxtExplanation.text = getString(R.string.capture_error_look_straight)

            captureFeedbackTxtTitle.setCheckedWithLeftDrawable(false)
        }

        toggleCaptureButtons(false)
        renderProgressBar(false)
    }

    private fun renderProgressBar(valid: Boolean) {
        binding?.apply {
            val progressColor =
                if (valid) UCR.color.capture_green
                else UCR.color.capture_grey

            captureProgress.progressColor = ContextCompat.getColor(
                requireContext(),
                progressColor
            )

            captureProgress.value = vm.userCaptures.size.toFloat()
        }
    }

    private fun toggleCaptureButtons(valid: Boolean) {
        binding?.captureFeedbackTxtTitle?.isClickable = valid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}



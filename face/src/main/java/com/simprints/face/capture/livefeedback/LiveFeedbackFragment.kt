package com.simprints.face.capture.livefeedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.detection.Face
import com.simprints.face.models.FaceDetection
import com.simprints.core.tools.extentions.setCheckedWithLeftDrawable
import com.simprints.uicomponents.models.Size
import kotlinx.android.synthetic.main.fragment_live_feedback.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.simprints.uicomponents.R as UCR

class LiveFeedbackFragment : Fragment() {
    private val mainVm: FaceCaptureViewModel by sharedViewModel()
    private val vm: LiveFeedbackFragmentViewModel by viewModel { parametersOf(mainVm) }
    private val languageResourcesHelper: LanguageResourcesHelper by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_live_feedback, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
        bindViewModel()
        capture_feedback_txt_title.setOnClickListener { vm.startCapture() }
        capture_progress.max = mainVm.samplesToCapture
        mainVm.startFaceDetection()
    }

    private fun setTextInLayout() {
        with(languageResourcesHelper) {
            capture_title.text = getString(R.string.title_confirmation)
            capture_feedback_txt_title.text = getString(R.string.capture_title_previewing)
        }
    }

    private fun bindViewModel() {
        vm.currentDetection.observe(viewLifecycleOwner, Observer {
            renderCurrentDetection(it)
//            renderDebugInfo(it.face)
        })

        vm.capturing.observe(viewLifecycleOwner, Observer { capturingState ->
            when (capturingState) {
                LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED -> {
                    capture_overlay.drawSemiTransparentTarget()
                    capture_title.text =
                        languageResourcesHelper.getString(R.string.title_preparation)
                    capture_feedback_txt_title.text =
                        languageResourcesHelper.getString(R.string.capture_prep_begin_btn)
                    toggleCaptureButtons(false)
                }
                LiveFeedbackFragmentViewModel.CapturingState.CAPTURING -> {
                    prepareCapturingStateColors()
                    capture_progress.isVisible = true
                    capture_title.text = languageResourcesHelper.getString(R.string.title_capturing)
                    capture_feedback_txt_title.text =
                        languageResourcesHelper.getString(R.string.capture_prep_begin_btn_capturing)
                    toggleCaptureButtons(false)
                }
                LiveFeedbackFragmentViewModel.CapturingState.FINISHED -> {
                    findNavController().navigate(R.id.action_liveFeedbackFragment_to_confirmationFragment)
                }
                LiveFeedbackFragmentViewModel.CapturingState.FINISHED_FAILED -> {
                    findNavController().navigate(R.id.action_liveFeedbackFragment_to_retryFragment)
                }
            }
        })

        lifecycleScope.launch {
            for (frame in mainVm.frameChannel) {
                vm.process(
                    frame, capture_overlay.rectInCanvas,
                    Size(
                        capture_overlay.width,
                        capture_overlay.height
                    )
                )
            }
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

    private fun prepareCapturingStateColors() {
        capture_overlay.drawWhiteTarget()

        capture_title.setTextColor(
            ContextCompat.getColor(requireContext(), UCR.color.capture_grey_blue)
        )
        capture_feedback_txt_explanation.setTextColor(
            ContextCompat.getColor(requireContext(), UCR.color.capture_grey_blue)
        )
    }

    private fun renderValidFace() {
        capture_feedback_txt_title.text = languageResourcesHelper.getString(R.string.capture_ready)
        capture_feedback_txt_explanation.text = null

        capture_feedback_txt_title.setCheckedWithLeftDrawable(
            true,
            ContextCompat.getDrawable(requireContext(), UCR.drawable.ic_checked_white_18dp)
        )
        toggleCaptureButtons(true)
    }

    private fun renderValidCapturingFace() {
        with(languageResourcesHelper) {
            capture_feedback_txt_title.text = getString(R.string.capture_prep_begin_btn_capturing)
            capture_feedback_txt_explanation.text = getString(R.string.capture_hold)
        }

        capture_feedback_txt_title.setCheckedWithLeftDrawable(
            true,
            ContextCompat.getDrawable(requireContext(), UCR.drawable.ic_checked_white_18dp)
        )

        renderProgressBar(true)
    }

    private fun renderFaceTooFar() {
        with(languageResourcesHelper) {
            capture_feedback_txt_title.text = getString(R.string.capture_title_face_too_far)
            capture_feedback_txt_explanation.text = getString(R.string.capture_error_face_too_far)
        }

        capture_feedback_txt_title.setCheckedWithLeftDrawable(false)
        toggleCaptureButtons(false)

        renderProgressBar(false)
    }

    private fun renderFaceTooClose() {
        with(languageResourcesHelper) {
            capture_feedback_txt_title.text = getString(R.string.capture_title_too_close)
            capture_feedback_txt_explanation.text = getString(R.string.capture_error_face_too_close)
        }

        capture_feedback_txt_title.setCheckedWithLeftDrawable(false)
        toggleCaptureButtons(false)

        renderProgressBar(false)
    }

    private fun renderNoFace() {
        with(languageResourcesHelper) {
            capture_feedback_txt_title.text = getString(R.string.capture_title_no_face)
            capture_feedback_txt_explanation.text = getString(R.string.capture_error_no_face)
        }

        capture_feedback_txt_title.setCheckedWithLeftDrawable(false)
        toggleCaptureButtons(false)

        renderProgressBar(false)
    }

    private fun renderFaceNotStraight() {
        with(languageResourcesHelper) {
            capture_feedback_txt_title.text = getString(R.string.capture_title_look_straight)
            capture_feedback_txt_explanation.text = getString(R.string.capture_error_look_straight)
        }

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
        capture_progress.value = vm.captures.size.toFloat()
    }

    private fun toggleCaptureButtons(valid: Boolean) {
        capture_feedback_txt_title.isClickable = valid
    }

}



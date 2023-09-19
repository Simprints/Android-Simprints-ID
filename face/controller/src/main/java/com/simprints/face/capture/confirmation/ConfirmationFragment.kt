package com.simprints.face.capture.confirmation

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent.Result.CONTINUE
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent.Result.RECAPTURE
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.databinding.FragmentConfirmationBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

/**
 * This class represents the screen the user is presented with once they have made a succesful capture
 * of a face
 */
@AndroidEntryPoint
class ConfirmationFragment : Fragment(R.layout.fragment_confirmation) {

    private val mainVM: FaceCaptureViewModel by activityViewModels()
    private val binding by viewBinding(FragmentConfirmationBinding::bind)

    @Inject
    lateinit var faceSessionEventsManager: FaceSessionEventsManager

    @Inject
    lateinit var faceTimeHelper: FaceTimeHelper

    private var startTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startTime = faceTimeHelper.now()
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        binding.apply(::setImageBitmapAndButtonClickListener)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            sendConfirmationEvent(RECAPTURE)
            mainVM.recapture()
        }
    }

    private fun setTextInLayout() {
        binding.apply {
            faceConfirmTitle.text = getString(IDR.string.title_confirmation)
            confirmationTxt.text = getString(IDR.string.captured_successfully)
            recaptureBtn.text = getString(IDR.string.btn_recapture)
            confirmationBtn.text = getString(IDR.string.btn_finish)
        }
    }

    private fun setImageBitmapAndButtonClickListener(binding: FragmentConfirmationBinding) {
        binding.confirmationImg.setImageBitmap(mainVM.faceDetections.first().bitmap)
        binding.confirmationBtn.setOnClickListener {
            sendConfirmationEvent(CONTINUE)
            mainVM.flowFinished()
        }
        binding.recaptureBtn.setOnClickListener {
            sendConfirmationEvent(RECAPTURE)
            mainVM.recapture()
        }
    }

    private fun sendConfirmationEvent(result: FaceCaptureConfirmationEvent.Result) {
        faceSessionEventsManager.addEventInBackground(
            FaceCaptureConfirmationEvent(startTime, faceTimeHelper.now(), result)
        )
    }
}

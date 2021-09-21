package com.simprints.face.capture.confirmation

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent.Result.CONTINUE
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent.Result.RECAPTURE
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.databinding.FragmentConfirmationBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConfirmationFragment: Fragment(R.layout.fragment_confirmation) {

    private val mainVM: FaceCaptureViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentConfirmationBinding::bind)

    private val faceSessionEventsManager: FaceSessionEventsManager by inject()
    private val faceTimeHelper: FaceTimeHelper by inject()
    private val startTime = faceTimeHelper.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
            faceConfirmTitle.text = getString(R.string.title_confirmation)
            confirmationTxt.text = getString(R.string.captured_successfully)
            recaptureBtn.text = getString(R.string.btn_recapture)
            confirmationBtn.text = getString(R.string.btn_finish)
        }
    }

    private fun setImageBitmapAndButtonClickListener(binding: FragmentConfirmationBinding) {
        binding.confirmationImg.setImageBitmap(mainVM.faceDetections.first().frame.toBitmap())
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

package com.simprints.face.capture.confirmation

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent.Result.CONTINUE
import com.simprints.face.controllers.core.events.model.FaceCaptureConfirmationEvent.Result.RECAPTURE
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import kotlinx.android.synthetic.main.fragment_confirmation.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConfirmationFragment : Fragment(R.layout.fragment_confirmation) {

    private val mainVM: FaceCaptureViewModel by sharedViewModel()
    private val faceSessionEventsManager: FaceSessionEventsManager by inject()
    private val faceTimeHelper: FaceTimeHelper by inject()
    private val startTime = faceTimeHelper.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
        confirmation_img.setImageBitmap(mainVM.faceDetections.first().frame.toBitmap())
        confirmation_btn.setOnClickListener {
            sendConfirmationEvent(CONTINUE)
            mainVM.flowFinished()
        }
        recapture_btn.setOnClickListener {
            sendConfirmationEvent(RECAPTURE)
            mainVM.recapture()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            sendConfirmationEvent(RECAPTURE)
            mainVM.recapture()
        }
    }

    private fun setTextInLayout() {
        face_confirm_title.text = getString(R.string.title_confirmation)
        confirmation_txt.text = getString(R.string.captured_successfully)
        recapture_btn.text = getString(R.string.btn_recapture)
        confirmation_btn.text = getString(R.string.btn_finish)
    }

    private fun sendConfirmationEvent(result: FaceCaptureConfirmationEvent.Result) {
        faceSessionEventsManager.addEventInBackground(
            FaceCaptureConfirmationEvent(startTime, faceTimeHelper.now(), result)
        )
    }
}

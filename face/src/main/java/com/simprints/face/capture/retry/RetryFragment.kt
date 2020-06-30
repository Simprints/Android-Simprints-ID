package com.simprints.face.capture.retry

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.androidResources.FaceAndroidResourcesHelper
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceCaptureRetryEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import kotlinx.android.synthetic.main.fragment_retry.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class RetryFragment : Fragment(R.layout.fragment_retry) {

    private val mainVM: FaceCaptureViewModel by sharedViewModel()
    private val androidResourcesHelper: FaceAndroidResourcesHelper by inject()
    private val faceSessionEventsManager: FaceSessionEventsManager by inject()
    private val faceTimeHelper: FaceTimeHelper by inject()
    private val startTime = faceTimeHelper.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
        retry_btn.setOnClickListener {
            sendRetryEvent()
            mainVM.handleRetry(false)
        }
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
            retry_confirmation_title.text = getString(R.string.title_confirmation)
            retry_txt.text = getString(R.string.captured_unsuccessful)
            retry_tips_text.text = getString(R.string.retry_tips)
            retry_btn.text = getString(R.string.btn_capture_again)
        }
        if (!mainVM.canRetry) setUiForFailedRetries()
    }

    private fun setUiForFailedRetries() {
        layout_retry_tips.isVisible = false
        retry_btn.text = androidResourcesHelper.getString(R.string.btn_finish)
    }

    private fun sendRetryEvent() {
        faceSessionEventsManager.addEventInBackground(
            FaceCaptureRetryEvent(startTime, faceTimeHelper.now())
        )
    }

}

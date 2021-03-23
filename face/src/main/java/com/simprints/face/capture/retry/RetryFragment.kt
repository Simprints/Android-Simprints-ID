package com.simprints.face.capture.retry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceCaptureRetryEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.databinding.FragmentRetryBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class RetryFragment : Fragment() {

    private val mainVM: FaceCaptureViewModel by sharedViewModel()
    private var binding: FragmentRetryBinding? = null

    private val faceSessionEventsManager: FaceSessionEventsManager by inject()
    private val faceTimeHelper: FaceTimeHelper by inject()
    private val startTime = faceTimeHelper.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRetryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
        binding?.retryBtn?.setOnClickListener {
            sendRetryEvent()
            mainVM.handleRetry(false)
        }
    }

    private fun setTextInLayout() {
        binding?.apply {
            retryConfirmationTitle.text = getString(R.string.title_confirmation)
            retryTxt.text = getString(R.string.captured_unsuccessful)
            retryTipsText.text = getString(R.string.retry_tips)
            retryBtn.text = getString(R.string.btn_capture_again)
        }
        if (!mainVM.canRetry) setUiForFailedRetries()
    }

    private fun setUiForFailedRetries() {
        binding?.apply {
            layoutRetryTips.isVisible = false
            retryBtn.text = getString(R.string.btn_finish)
        }
    }

    private fun sendRetryEvent() {
        faceSessionEventsManager.addEventInBackground(
            FaceCaptureRetryEvent(startTime, faceTimeHelper.now())
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

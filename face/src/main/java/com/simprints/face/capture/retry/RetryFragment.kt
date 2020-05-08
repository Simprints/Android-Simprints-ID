package com.simprints.face.capture.retry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.androidResources.FaceAndroidResourcesHelper
import kotlinx.android.synthetic.main.fragment_retry.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class RetryFragment : Fragment() {

    private val mainVM: FaceCaptureViewModel by sharedViewModel()
    private val androidResourcesHelper: FaceAndroidResourcesHelper by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_retry, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
        retry_btn.setOnClickListener { mainVM.handleRetry() }
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

}

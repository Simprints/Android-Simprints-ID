package com.simprints.face.capture.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.androidResources.FaceAndroidResourcesHelper
import kotlinx.android.synthetic.main.fragment_confirmation.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConfirmationFragment : Fragment() {

    private val mainVM: FaceCaptureViewModel by sharedViewModel()
    private val androidResourcesHelper: FaceAndroidResourcesHelper by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_confirmation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
            face_confirm_title.text = getString(R.string.title_confirmation)
            confirmation_txt.text = getString(R.string.captured_successfully)
            confirmation_btn.text = getString(R.string.btn_finish)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        confirmation_img.setImageBitmap(mainVM.faceDetections.value?.first()?.frame?.toBitmap())
        confirmation_btn.setOnClickListener { mainVM.flowFinished() }
    }
}

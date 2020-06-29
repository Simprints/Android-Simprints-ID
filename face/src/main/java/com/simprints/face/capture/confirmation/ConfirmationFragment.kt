package com.simprints.face.capture.confirmation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import kotlinx.android.synthetic.main.fragment_confirmation.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConfirmationFragment : Fragment(R.layout.fragment_confirmation) {

    private val mainVM: FaceCaptureViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
        confirmation_img.setImageBitmap(mainVM.faceDetections.first().frame.toBitmap())
        confirmation_btn.setOnClickListener { mainVM.flowFinished() }
        recapture_btn.setOnClickListener { mainVM.recapture() }
    }

    private fun setTextInLayout() {
        face_confirm_title.text = getString(R.string.title_confirmation)
        confirmation_txt.text = getString(R.string.captured_successfully)
        recapture_btn.text = getString(R.string.btn_recapture)
        confirmation_btn.text = getString(R.string.btn_finish)
    }
}

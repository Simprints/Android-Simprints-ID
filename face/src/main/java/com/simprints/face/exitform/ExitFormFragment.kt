package com.simprints.face.exitform

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.androidResources.FaceAndroidResourcesHelper
import kotlinx.android.synthetic.main.fragment_exit_form.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.simprints.id.R as IDR

class ExitFormFragment : Fragment(R.layout.fragment_exit_form) {

    private val mainVm: FaceCaptureViewModel by sharedViewModel()
    private val vm: ExitFormViewModel by viewModel { parametersOf(mainVm) }
    private val androidResourcesHelper: FaceAndroidResourcesHelper by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
            whySkipBiometricsText.text = getString(R.string.why_did_you_skip_face_capture)
            rbReligiousConcerns.text = getString(IDR.string.refusal_religious_concerns)
            rbDataConcerns.text = getString(IDR.string.refusal_data_concerns)
            rbDoesNotHavePermission.text = getString(IDR.string.refusal_does_not_have_permission)
            rbAppNotWorking.text = getString(IDR.string.refusal_app_not_working)
            rbPersonNotPresent.text = getString(IDR.string.refusal_person_not_present)
            rbTooYoung.text = getString(IDR.string.refusal_too_young)
            rbOther.text = getString(IDR.string.refusal_other)
            exitFormText.hint = getString(IDR.string.hint_other_reason)
            btGoBack.text = getString(R.string.exit_form_return_to_face_capture)
            btSubmitExitForm.text = getString(IDR.string.button_submit)
        }
    }

}

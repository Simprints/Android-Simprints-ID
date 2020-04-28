package com.simprints.face.exitform

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.tools.extentions.showToast
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.androidResources.FaceAndroidResourcesHelper
import com.simprints.id.tools.textWatcherOnChange
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

    private val textWatcher = textWatcherOnChange {
        handleTextChangedInExitForm(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()
        setButtonListeners()
        setRadioGroupListener()
        setLayoutChangeListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        vm.requestReasonEvent.observe(viewLifecycleOwner, LiveDataEventObserver {
            setFocusOnExitReasonAndDisableSubmit()
        })
        vm.requestSelectOptionEvent.observe(viewLifecycleOwner, LiveDataEventObserver {
            requireContext().showToast(androidResourcesHelper.getString(IDR.string.refusal_toast_select_option_submit))
        })
        vm.requestFormSubmitEvent.observe(viewLifecycleOwner, LiveDataEventObserver {
            requireContext().showToast(androidResourcesHelper.getString(IDR.string.refusal_toast_submit))
        })
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

    private fun setButtonListeners() {
        btGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
        btSubmitExitForm.setOnClickListener {
            vm.submitExitForm(getExitFormText())
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            vm.handleBackButton()
        }
    }

    private fun setRadioGroupListener() {
        exitFormRadioGroup.setOnCheckedChangeListener { _, optionIdentifier ->
            exitFormText.removeTextChangedListener(textWatcher)
            enableSubmitButton()
            enableRefusalText()
            handleRadioOptionIdentifierClick(optionIdentifier)
        }
    }

    private fun handleRadioOptionIdentifierClick(optionIdentifier: Int) {
        when (optionIdentifier) {
            R.id.rbReligiousConcerns -> vm.handleReligiousConcernsRadioClick()
            R.id.rbDataConcerns -> vm.handleDataConcernsRadioClick()
            R.id.rbPersonNotPresent -> vm.handlePersonNotPresentRadioClick()
            R.id.rbTooYoung -> vm.handleTooYoungRadioClick()
            R.id.rbDoesNotHavePermission -> vm.handleDoesNotHavePermissionRadioClick()
            R.id.rbAppNotWorking -> vm.handleAppNotWorkingRadioClick()
            R.id.rbOther -> vm.handleOtherRadioOptionClick()
        }
    }

    //Changes in the layout occur when the keyboard shows up
    private fun setLayoutChangeListener() {
        faceExitFormScrollView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            faceExitFormScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun handleTextChangedInExitForm(exitFormText: String) {
        if (exitFormText.isNotBlank()) {
            enableSubmitButton()
        } else {
            disableSubmitButton()
        }
    }

    private fun enableSubmitButton() {
        btSubmitExitForm.isEnabled = true
    }

    private fun disableSubmitButton() {
        btSubmitExitForm.isEnabled = false
    }

    private fun enableRefusalText() {
        exitFormText.isEnabled = true
    }

    private fun getExitFormText() = exitFormText.text.toString()

    private fun setFocusOnExitReasonAndDisableSubmit() {
        btSubmitExitForm.isEnabled = false
        exitFormText.requestFocus()
        setTextChangeListenerOnExitText()
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(exitFormText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnExitText() {
        exitFormText.addTextChangedListener(textWatcher)
    }

}

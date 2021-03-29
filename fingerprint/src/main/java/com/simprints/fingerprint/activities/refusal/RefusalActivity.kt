package com.simprints.fingerprint.activities.refusal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.core.tools.extentions.hideKeyboard
import com.simprints.fingerprint.databinding.ActivityRefusalBinding
import com.simprints.fingerprint.tools.extensions.showToast
import org.jetbrains.anko.inputMethodManager
import org.jetbrains.anko.sdk27.coroutines.onLayoutChange
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class RefusalActivity : FingerprintActivity(), RefusalContract.View {

    private lateinit var binding: ActivityRefusalBinding
    override val viewPresenter: RefusalContract.Presenter by inject{ parametersOf(this) }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(refusalTextCharSequence: CharSequence, start: Int, before: Int, count: Int) {
            viewPresenter.handleChangesInRefusalText(refusalTextCharSequence.toString())
        }

        override fun afterTextChanged(s: Editable) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRefusalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        title = getString(R.string.refusal_label)

        setTextInLayout()

        setButtonClickListeners()
        setLayoutChangeListeners()
        setRadioGroupListener()
    }

    private fun setTextInLayout() {
        binding.apply {
            whySkipFingerprintingText.text = getString(R.string.why_did_you_skip_fingerprinting)
            rbReligiousConcerns.text = getString(R.string.refusal_religious_concerns)
            rbDataConcerns.text = getString(R.string.refusal_data_concerns)
            rbDoesNotHavePermission.text = getString(R.string.refusal_does_not_have_permission)
            rbAppNotWorking.text = getString(R.string.refusal_app_not_working)
            rbPersonNotPresent.text = getString(R.string.refusal_person_not_present)
            rbTooYoung.text = getString(R.string.refusal_too_young)
            rbOther.text = getString(R.string.refusal_other)
            refusalText.hint = getString(R.string.hint_other_reason)
            btScanFingerprints.text = getString(R.string.button_scan_prints)
            btSubmitRefusalForm.text = getString(R.string.button_submit)
        }
    }

    private fun setButtonClickListeners() {
        binding.btSubmitRefusalForm.setOnClickListener { viewPresenter.handleSubmitButtonClick(getRefusalText()) }
        binding.btScanFingerprints.setOnClickListener { viewPresenter.handleScanFingerprintsClick() }
    }

    //Changes in the layout occur when the keyboard shows up
    private fun setLayoutChangeListeners() {
        binding.refusalScrollView.onLayoutChange { _, _, _, _,
                                           _, _, _, _, _ ->
            viewPresenter.handleLayoutChange()
        }
    }

    private fun setRadioGroupListener() {
        binding.refusalRadioGroup.setOnCheckedChangeListener { _, optionIdentifier ->
            binding.refusalText.removeTextChangedListener(textWatcher)
            viewPresenter.handleRadioOptionCheckedChange()
            handleRadioOptionIdentifierClick(optionIdentifier)
        }
    }

    private fun handleRadioOptionIdentifierClick(optionIdentifier: Int) {
        when (optionIdentifier) {
            R.id.rbReligiousConcerns -> viewPresenter.handleReligiousConcernsRadioClick()
            R.id.rbDataConcerns -> viewPresenter.handleDataConcernsRadioClick()
            R.id.rbPersonNotPresent -> viewPresenter.handlePersonNotPresentRadioClick()
            R.id.rbTooYoung -> viewPresenter.handleTooYoungRadioClick()
            R.id.rbDoesNotHavePermission -> viewPresenter.handleDoesNotHavePermissionRadioClick()
            R.id.rbAppNotWorking -> viewPresenter.handleAppNotWorkingRadioClick()
            R.id.rbOther -> viewPresenter.handleOtherRadioOptionClick()
        }
    }

    override fun scrollToBottom() {
        binding.refusalScrollView.post {
            binding.refusalScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun enableSubmitButton() {
        binding.btSubmitRefusalForm.isEnabled = true
    }

    override fun disableSubmitButton() {
        binding.btSubmitRefusalForm.isEnabled = false
    }

    override fun enableRefusalText() {
        binding.refusalText.isEnabled = true
    }

    override fun setFocusOnRefusalReasonAndDisableSubmit() {
        binding.btSubmitRefusalForm.isEnabled = false
        binding.refusalText.requestFocus()
        setTextChangeListenerOnRefusalText()
        inputMethodManager.showSoftInput(binding.refusalText, SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnRefusalText() {
        binding.refusalText.addTextChangedListener(textWatcher)
    }

    override fun setResultAndFinish(activityResult: Int, refusalResult: RefusalTaskResult) {
        setResult(activityResult, getIntentForResultData(refusalResult))
        finish()
    }

    private fun getIntentForResultData(refusalResult: RefusalTaskResult) =
        Intent().putExtra(
            RefusalTaskResult.BUNDLE_KEY,
            refusalResult)

    private fun getRefusalText() = binding.refusalText.text.toString()

    override fun onBackPressed() {
        viewPresenter.handleOnBackPressed()
    }

    override fun isSubmitButtonEnabled() = binding.btSubmitRefusalForm.isEnabled

    override fun showToastForFormSubmit() {
        showToast(getString(R.string.refusal_toast_submit))
    }

    override fun showToastForSelectOptionAndSubmit() {
        showToast(getString(R.string.refusal_toast_select_option_submit))
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }
}

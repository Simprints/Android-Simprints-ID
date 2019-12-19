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
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_refusal.*
import org.jetbrains.anko.inputMethodManager
import org.jetbrains.anko.sdk27.coroutines.onLayoutChange
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class RefusalActivity : FingerprintActivity(), RefusalContract.View {

    override val viewPresenter: RefusalContract.Presenter by inject{ parametersOf(this) }

    val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()

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

        setContentView(R.layout.activity_refusal)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setTextInLayout()

        setButtonClickListeners()
        setLayoutChangeListeners()
        setRadioGroupListener()
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
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
        btSubmitRefusalForm.setOnClickListener { viewPresenter.handleSubmitButtonClick(getRefusalText()) }
        btScanFingerprints.setOnClickListener { viewPresenter.handleScanFingerprintsClick() }
    }

    //Changes in the layout occur when the keyboard shows up
    private fun setLayoutChangeListeners() {
        refusalScrollView.onLayoutChange { _, _, _, _,
                                           _, _, _, _, _ ->
            viewPresenter.handleLayoutChange()
        }
    }

    private fun setRadioGroupListener() {
        refusalRadioGroup.setOnCheckedChangeListener { _, optionIdentifier ->
            refusalText.removeTextChangedListener(textWatcher)
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
        refusalScrollView.post {
            refusalScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun enableSubmitButton() {
        btSubmitRefusalForm.isEnabled = true
    }

    override fun disableSubmitButton() {
        btSubmitRefusalForm.isEnabled = false
    }

    override fun enableRefusalText() {
        refusalText.isEnabled = true
    }

    override fun setFocusOnRefusalReasonAndDisableSubmit() {
        btSubmitRefusalForm.isEnabled = false
        refusalText.requestFocus()
        setTextChangeListenerOnRefusalText()
        inputMethodManager.showSoftInput(refusalText, SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnRefusalText() {
        refusalText.addTextChangedListener(textWatcher)
    }

    override fun setResultAndFinish(activityResult: Int, refusalResult: RefusalTaskResult) {
        setResult(activityResult, getIntentForResultData(refusalResult))
        finish()
    }

    private fun getIntentForResultData(refusalResult: RefusalTaskResult) =
        Intent().putExtra(
            RefusalTaskResult.BUNDLE_KEY,
            refusalResult)

    private fun getRefusalText() = refusalText.text.toString()

    override fun onBackPressed() {
        viewPresenter.handleOnBackPressed()
    }

    override fun isSubmitButtonEnabled() = btSubmitRefusalForm.isEnabled

    override fun showToastForFormSubmit() {
        showToast(androidResourcesHelper.getString(R.string.refusal_toast_submit))
    }

    override fun showToastForSelectOptionAndSubmit() {
        showToast(androidResourcesHelper.getString(R.string.refusal_toast_select_option_submit))
    }
}

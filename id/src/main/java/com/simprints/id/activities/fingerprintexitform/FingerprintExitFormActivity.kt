package com.simprints.id.activities.fingerprintexitform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.extentions.textWatcherOnChange
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult.Action
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult.Answer
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.databinding.ActivityFingerprintExitFormBinding
import com.simprints.id.exitformhandler.ExitFormResult.Companion.EXIT_FORM_BUNDLE_KEY
import com.simprints.id.tools.extensions.onLayoutChange
import com.simprints.id.tools.extensions.showToast
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import splitties.systemservices.inputMethodManager
import javax.inject.Inject
import com.simprints.infraresources.R as IDR

class FingerprintExitFormActivity : BaseSplitActivity() {

    private lateinit var viewModel: FingerprintExitFormViewModel
    private val binding by viewBinding(ActivityFingerprintExitFormBinding::inflate)

    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var fingerprintExitFormViewModelFactory: FingerprintExitFormViewModelFactory

    private var fingerprintExitFormStartTime: Long = 0
    private var fingerprintExitFormReason = FingerprintExitFormReason.OTHER

    private val textWatcher = textWatcherOnChange {
        handleTextChangedInExitForm(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        injectDependencies()

        setTextInLayout()

        viewModel = ViewModelProvider(this, fingerprintExitFormViewModelFactory)
            .get(FingerprintExitFormViewModel::class.java)
        fingerprintExitFormStartTime = timeHelper.now()

        setRadioGroupListener()
        setLayoutChangeListener()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setTextInLayout() {
        binding.apply {
            whySkipFingerprintText.text = getString(IDR.string.why_did_you_skip_fingerprinting)
            fingerprintRbReligiousConcerns.text = getString(IDR.string.refusal_religious_concerns)
            fingerprintRbDataConcerns.text = getString(IDR.string.refusal_data_concerns)
            fingerprintRbDoesNotHavePermission.text = getString(IDR.string.refusal_does_not_have_permission)
            fingerprintRbAppNotWorking.text = getString(IDR.string.refusal_app_not_working)
            fingerprintRbPersonNotPresent.text = getString(IDR.string.refusal_person_not_present)
            fingerprintRbTooYoung.text = getString(IDR.string.refusal_too_young)
            fingerprintRbOther.text = getString(IDR.string.refusal_other)
            fingerprintExitFormText.hint = getString(IDR.string.hint_other_reason)
            fingerprintBtSubmitExitForm.text = getString(IDR.string.button_submit)
            fingerprintBtGoBack.text = getString(IDR.string.button_scan_prints)
        }
    }

    private fun setRadioGroupListener() {
        binding.fingerprintExitFormRadioGroup.setOnCheckedChangeListener { _, optionIdentifier ->
            binding.fingerprintExitFormText.removeTextChangedListener(textWatcher)
            enableSubmitButton()
            enableFingerprintExitFormText()
            handleRadioOptionIdentifierClick(optionIdentifier)
        }
    }

    //Changes in the layout occur when the keyboard shows up
    private fun setLayoutChangeListener() {
        binding.fingerprintExitFormScrollView.onLayoutChange { _, _, _, _, _, _, _, _, _ ->
            binding.fingerprintExitFormScrollView.fullScroll(View.FOCUS_DOWN)
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
        binding.fingerprintBtSubmitExitForm.isEnabled = true
    }

    private fun disableSubmitButton() {
        binding.fingerprintBtSubmitExitForm.isEnabled = false
    }

    private fun enableFingerprintExitFormText() {
        binding.fingerprintExitFormText.isEnabled = true
    }

    private fun handleRadioOptionIdentifierClick(optionIdentifier: Int) {
        when (optionIdentifier) {
            R.id.fingerprintRbReligiousConcerns -> {
                fingerprintExitFormReason = FingerprintExitFormReason.REFUSED_RELIGION
                logRadioOptionForCrashReport("Religious Concerns")
            }
            R.id.fingerprintRbDataConcerns -> {
                fingerprintExitFormReason = FingerprintExitFormReason.REFUSED_DATA_CONCERNS
                logRadioOptionForCrashReport("Data Concerns")
            }
            R.id.fingerprintRbPersonNotPresent -> {
                fingerprintExitFormReason = FingerprintExitFormReason.REFUSED_NOT_PRESENT
                logRadioOptionForCrashReport("Person not present")
            }
            R.id.fingerprintRbTooYoung -> {
                fingerprintExitFormReason = FingerprintExitFormReason.REFUSED_YOUNG
                logRadioOptionForCrashReport("Too young")
            }
            R.id.fingerprintRbDoesNotHavePermission -> {
                fingerprintExitFormReason = FingerprintExitFormReason.REFUSED_PERMISSION
                logRadioOptionForCrashReport("Does not have permission")
            }
            R.id.fingerprintRbAppNotWorking -> {
                fingerprintExitFormReason = FingerprintExitFormReason.SCANNER_NOT_WORKING
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("App not working")
            }
            R.id.fingerprintRbOther -> {
                fingerprintExitFormReason = FingerprintExitFormReason.OTHER
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("Other")
            }
        }
    }

    private fun getExitFormText() = binding.fingerprintExitFormText.text.toString()

    private fun setFocusOnExitReasonAndDisableSubmit() {
        binding.fingerprintBtSubmitExitForm.isEnabled = false
        binding.fingerprintExitFormText.requestFocus()
        setTextChangeListenerOnExitText()
        inputMethodManager.showSoftInput(binding.fingerprintExitFormText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnExitText() {
        binding.fingerprintExitFormText.addTextChangedListener(textWatcher)
    }


    fun handleScanFingerprintsClick(@Suppress("UNUSED_PARAMETER")view: View) {
        setResultAndFinish(Action.SCAN_FINGERPRINTS)
    }

    fun handleSubmitClick(@Suppress("UNUSED_PARAMETER")view: View) {
        viewModel.addExitFormEvent(fingerprintExitFormStartTime, timeHelper.now(),
            getExitFormText(), fingerprintExitFormReason)
        setResultAndFinish(Action.SUBMIT)
    }

    private fun setResultAndFinish(exitFormActivityAction: Action) {
        setResult(Activity.RESULT_OK, getIntentForResult(exitFormActivityAction))
        finish()
    }

    private fun getIntentForResult(exitFormActivityAction: Action) =
        Intent().putExtra(EXIT_FORM_BUNDLE_KEY, buildExitFormResult(exitFormActivityAction))

    private fun buildExitFormResult(exitFormActivityAction: Action) =
        FingerprintExitFormActivityResult(exitFormActivityAction,
            Answer(fingerprintExitFormReason, getExitFormText()))


    override fun onBackPressed() {
        if (binding.fingerprintBtSubmitExitForm.isEnabled) {
            showToast(IDR.string.refusal_toast_submit)
        } else {
            showToast(IDR.string.refusal_toast_select_option_submit)
        }
    }

    private fun logRadioOptionForCrashReport(option: String) {
        logMessageForCrashReport("Radio option $option clicked")
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(CrashReportTag.REFUSAL.name).i(message)
    }
}

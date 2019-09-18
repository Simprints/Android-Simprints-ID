package com.simprints.id.activities.fingerprintexitform

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormResult
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormResult.Companion.FINGERPRINT_EXIT_FORM_BUNDLE_KEY
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormResult.Companion.FINGERPRINT_EXIT_FORM_RESULT_CODE_SCAN_FINGERPRINTS
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormResult.Companion.FINGERPRINT_EXIT_FORM_RESULT_CODE_SUBMIT
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.showToast
import com.simprints.id.tools.textWatcherOnChange
import kotlinx.android.synthetic.main.activity_fingerprint_exit_form.*
import org.jetbrains.anko.inputMethodManager
import javax.inject.Inject

class FingerprintExitFormActivity : AppCompatActivity() {

    private lateinit var viewModel: FingerprintExitFormViewModel

    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var fingerprintExitFormViewModelFactory: FingerprintExitFormViewModelFactory

    private var fingerprintExitFormStartTime: Long = 0
    private var fingerprintExitFormReason = FingerprintExitFormReason.OTHER

    private val textWatcher = textWatcherOnChange {
        handleTextChangedInExitForm(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint_exit_form)

        injectDependencies()

        viewModel = ViewModelProviders.of(this, fingerprintExitFormViewModelFactory)
            .get(FingerprintExitFormViewModel::class.java)
        fingerprintExitFormStartTime = timeHelper.now()

        setRadioGroupListener()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setRadioGroupListener() {
        fingerprintExitFormRadioGroup.setOnCheckedChangeListener { _, optionIdentifier ->
            fingerprintExitFormText.removeTextChangedListener(textWatcher)
            enableSubmitButton()
            enableFingerprintExitFormText()
            handleRadioOptionIdentifierClick(optionIdentifier)
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
        fingerprintBtSubmitExitForm.isEnabled = true
    }

    private fun disableSubmitButton() {
        fingerprintBtSubmitExitForm.isEnabled = false
    }

    private fun enableFingerprintExitFormText() {
        fingerprintExitFormText.isEnabled = true
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

    private fun getExitFormText() = fingerprintExitFormText.text.toString()

    private fun setFocusOnExitReasonAndDisableSubmit() {
        fingerprintBtSubmitExitForm.isEnabled = false
        fingerprintExitFormText.requestFocus()
        setTextChangeListenerOnExitText()
        inputMethodManager.showSoftInput(fingerprintExitFormText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnExitText() {
        fingerprintExitFormText.addTextChangedListener(textWatcher)
    }


    fun handleScanFingerprintsClick(@Suppress("UNUSED_PARAMETER")view: View) {
        setResultAndFinish(FINGERPRINT_EXIT_FORM_RESULT_CODE_SCAN_FINGERPRINTS,
            FingerprintExitFormResult.Action.SCAN_FINGERPRINTS)
    }

    fun handleSubmitClick(@Suppress("UNUSED_PARAMETER")view: View) {
        viewModel.addExitFormEvent(fingerprintExitFormStartTime, timeHelper.now(),
            getExitFormText(), fingerprintExitFormReason)
        setResultAndFinish(FINGERPRINT_EXIT_FORM_RESULT_CODE_SUBMIT, FingerprintExitFormResult.Action.SUBMIT)
    }

    private fun setResultAndFinish(resultCode: Int, exitFormAction: FingerprintExitFormResult.Action) {
        setResult(resultCode, getIntentForResult(exitFormAction))
        finish()
    }

    private fun getIntentForResult(exitFormAction: FingerprintExitFormResult.Action) =
        Intent().putExtra(FINGERPRINT_EXIT_FORM_BUNDLE_KEY, buildExitFormResult(exitFormAction))

    private fun buildExitFormResult(exitFormAction: FingerprintExitFormResult.Action) =
        FingerprintExitFormResult(exitFormAction,
            FingerprintExitFormResult.Answer(fingerprintExitFormReason, getExitFormText()))


    override fun onBackPressed() {
        if (fingerprintBtSubmitExitForm.isEnabled) {
            showToast(R.string.refusal_toast_submit)
        } else {
            showToast(R.string.refusal_toast_select_option_submit)
        }
    }

    private fun logRadioOptionForCrashReport(option: String) {
        logMessageForCrashReport("Radio option $option clicked")
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.REFUSAL, CrashReportTrigger.UI, message = message)
    }
}

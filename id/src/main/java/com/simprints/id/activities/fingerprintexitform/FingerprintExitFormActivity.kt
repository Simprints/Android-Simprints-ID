package com.simprints.id.activities.fingerprintexitform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult.Action
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult.Answer
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.exitformhandler.ExitFormResult.Companion.EXIT_FORM_BUNDLE_KEY
import com.simprints.id.tools.AndroidResourcesHelper
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
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    private var fingerprintExitFormStartTime: Long = 0
    private var fingerprintExitFormReason = FingerprintExitFormReason.OTHER

    private val textWatcher = textWatcherOnChange {
        handleTextChangedInExitForm(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint_exit_form)

        injectDependencies()

        viewModel = ViewModelProvider(this, fingerprintExitFormViewModelFactory)
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
        if (fingerprintBtSubmitExitForm.isEnabled) {
            showToast(androidResourcesHelper, R.string.refusal_toast_submit)
        } else {
            showToast(androidResourcesHelper, R.string.refusal_toast_select_option_submit)
        }
    }

    private fun logRadioOptionForCrashReport(option: String) {
        logMessageForCrashReport("Radio option $option clicked")
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.REFUSAL, CrashReportTrigger.UI, message = message)
    }
}

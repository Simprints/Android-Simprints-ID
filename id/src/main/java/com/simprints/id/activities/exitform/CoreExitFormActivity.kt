package com.simprints.id.activities.exitform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.exitform.result.CoreExitFormResult
import com.simprints.id.activities.exitform.result.CoreExitFormResult.Action.GO_BACK
import com.simprints.id.activities.exitform.result.CoreExitFormResult.Action.SUBMIT
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.exitform.ExitFormReason.*
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_core_exit_form.*
import org.jetbrains.anko.inputMethodManager
import javax.inject.Inject

class CoreExitFormActivity : AppCompatActivity() {

    private lateinit var viewModel: CoreExitFormViewModel

    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var coreExitFormViewModelFactory: CoreExitFormViewModelFactory

    private var exitFormStartTime: Long = 0
    private var exitFormReason = OTHER

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(exitFormTextCharSequence: CharSequence, start: Int, before: Int, count: Int) {
            handleTextChangedInExitForm(exitFormTextCharSequence.toString())
        }

        override fun afterTextChanged(s: Editable) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core_exit_form)

        injectDependencies()

        viewModel = ViewModelProviders.of(this, coreExitFormViewModelFactory).get(CoreExitFormViewModel::class.java)
        exitFormStartTime = timeHelper.now()

        setRadioGroupListener()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setRadioGroupListener() {
        refusalRadioGroup.setOnCheckedChangeListener { _, optionIdentifier ->
            refusalText.removeTextChangedListener(textWatcher)
            enableSubmitButton()
            enableRefusalText()
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
        btSubmitRefusalForm.isEnabled = true
    }

    private fun disableSubmitButton() {
        btSubmitRefusalForm.isEnabled = false
    }

    private fun enableRefusalText() {
        refusalText.isEnabled = true
    }

    private fun handleRadioOptionIdentifierClick(optionIdentifier: Int) {
        when (optionIdentifier) {
            R.id.rbReligiousConcerns -> {
                exitFormReason = REFUSED_RELIGION
                logRadioOptionForCrashReport("Religious Concerns")
            }
            R.id.rbDataConcerns -> {
                exitFormReason = REFUSED_DATA_CONCERNS
                logRadioOptionForCrashReport("Data Concerns")
            }
            R.id.rbPersonNotPresent -> {
                exitFormReason = REFUSED_NOT_PRESENT
                logRadioOptionForCrashReport("Person not present")
            }
            R.id.rbTooYoung -> {
                exitFormReason = REFUSED_YOUNG
                logRadioOptionForCrashReport("Too young")
            }
            R.id.rbDoesNotHavePermission -> {
                exitFormReason = REFUSED_PERMISSION
                logRadioOptionForCrashReport("Does not have permission")
            }
            R.id.rbAppNotWorking -> {
                exitFormReason = SCANNER_NOT_WORKING
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("App not working")
            }
            R.id.rbOther -> {
                exitFormReason = OTHER
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("Other")
            }
        }
    }

    fun handleGoBackClick(@Suppress("UNUSED_PARAMETER")view: View) {
        setResultAndFinish(GO_BACK)
    }

    fun handleSubmitClick(@Suppress("UNUSED_PARAMETER")view: View) {
        viewModel.addExitFormEvent(exitFormStartTime, timeHelper.now(), getExitFormText(), exitFormReason)
        setResultAndFinish(SUBMIT)
    }

    private fun setResultAndFinish(exitFormAction: CoreExitFormResult.Action) {
        setResult(Activity.RESULT_OK, getIntentForResult(exitFormAction))
        finish()
    }

    private fun getExitFormText() = refusalText.text.toString()

    private fun getIntentForResult(exitFormAction: CoreExitFormResult.Action) =
        Intent().putExtra(CoreExitFormResult.BUNDLE_KEY, buildExitFormResult(exitFormAction))

    private fun buildExitFormResult(exitFormAction: CoreExitFormResult.Action) =
        CoreExitFormResult(exitFormAction,
        CoreExitFormResult.Answer(exitFormReason, getExitFormText()))

    private fun setFocusOnExitReasonAndDisableSubmit() {
        btSubmitRefusalForm.isEnabled = false
        refusalText.requestFocus()
        setTextChangeListenerOnExitText()
        inputMethodManager.showSoftInput(refusalText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnExitText() {
        refusalText.addTextChangedListener(textWatcher)
    }

    override fun onBackPressed() {
        if (btSubmitRefusalForm.isEnabled) {
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

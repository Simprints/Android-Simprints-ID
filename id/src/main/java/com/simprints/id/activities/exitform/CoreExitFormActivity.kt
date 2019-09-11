package com.simprints.id.activities.exitform

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
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
                viewModel.handleReligiousConcernsRadioClick()
                logRadioOptionForCrashReport("Religious Concerns")
            }
            R.id.rbDataConcerns -> {
                viewModel.handleDataConcernsRadioClick()
                logRadioOptionForCrashReport("Data Concerns")
            }
            R.id.rbPersonNotPresent -> {
                viewModel.handlePersonNotPresentRadioClick()
                logRadioOptionForCrashReport("Person not present")
            }
            R.id.rbTooYoung -> {
                viewModel.handleTooYoungRadioClick()
                logRadioOptionForCrashReport("Too young")
            }
            R.id.rbDoesNotHavePermission -> {
                viewModel.handleDoesNotHavePermissionRadioClick()
                logRadioOptionForCrashReport("Does not have permission")
            }
            R.id.rbAppNotWorking -> {
                viewModel.handleAppNotWorkingRadioClick()
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("App not working")
            }
            R.id.rbOther -> {
                viewModel.handleOtherRadioOptionClick()
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("Other")
            }
        }
    }

    fun handleGoBackClick(@Suppress("UNUSED_PARAMETER")view: View) {
        
    }

    fun handleSubmitClick(@Suppress("UNUSED_PARAMETER")view: View) {
        viewModel.addExitFormEvent(exitFormStartTime, timeHelper.now(), getExitFormText())
        //Set Result And Finish
    }

    private fun getExitFormText() = refusalText.text.toString()

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

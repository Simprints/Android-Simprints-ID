package com.simprints.id.activities.coreexitform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.coreexitform.result.CoreExitFormActivityResult
import com.simprints.id.activities.coreexitform.result.CoreExitFormActivityResult.Action.GO_BACK
import com.simprints.id.activities.coreexitform.result.CoreExitFormActivityResult.Action.SUBMIT
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.exitform.CoreExitFormReason.*
import com.simprints.id.exitformhandler.ExitFormResult.Companion.EXIT_FORM_BUNDLE_KEY
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.showToast
import com.simprints.id.tools.textWatcherOnChange
import kotlinx.android.synthetic.main.activity_core_exit_form.*
import org.jetbrains.anko.inputMethodManager
import javax.inject.Inject

class CoreExitFormActivity : AppCompatActivity() {

    private lateinit var viewModel: CoreExitFormViewModel

    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var coreExitFormViewModelFactory: CoreExitFormViewModelFactory
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    private var exitFormStartTime: Long = 0
    private var exitFormReason = OTHER

    private val textWatcher = textWatcherOnChange {
        handleTextChangedInExitForm(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core_exit_form)

        injectDependencies()

        setTextInLayout()

        viewModel = ViewModelProvider(this, coreExitFormViewModelFactory).get(CoreExitFormViewModel::class.java)
        exitFormStartTime = timeHelper.now()

        setRadioGroupListener()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
            whySkipBiometricsText.text = getString(R.string.why_did_you_skip_biometrics)
            rbReligiousConcerns.text = getString(R.string.refusal_religious_concerns)
            rbDataConcerns.text = getString(R.string.refusal_data_concerns)
            rbDoesNotHavePermission.text = getString(R.string.refusal_does_not_have_permission)
            rbAppNotWorking.text = getString(R.string.refusal_app_not_working)
            rbPersonNotPresent.text = getString(R.string.refusal_person_not_present)
            rbTooYoung.text = getString(R.string.refusal_too_young)
            rbOther.text = getString(R.string.refusal_other)
            exitFormText.hint = getString(R.string.hint_other_reason)
            btSubmitExitForm.text = getString(R.string.button_submit)
            btGoBack.text = getString(R.string.exit_form_return_to_simprints)
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

    private fun setResultAndFinish(exitFormActivityAction: CoreExitFormActivityResult.Action) {
        setResult(Activity.RESULT_OK, getIntentForResult(exitFormActivityAction))
        finish()
    }

    private fun getExitFormText() = exitFormText.text.toString()

    private fun getIntentForResult(exitFormActivityAction: CoreExitFormActivityResult.Action) =
        Intent().putExtra(EXIT_FORM_BUNDLE_KEY, buildExitFormResult(exitFormActivityAction))

    private fun buildExitFormResult(exitFormActivityAction: CoreExitFormActivityResult.Action) =
        CoreExitFormActivityResult(exitFormActivityAction,
            CoreExitFormActivityResult.Answer(exitFormReason, getExitFormText()))

    private fun setFocusOnExitReasonAndDisableSubmit() {
        btSubmitExitForm.isEnabled = false
        exitFormText.requestFocus()
        setTextChangeListenerOnExitText()
        inputMethodManager.showSoftInput(exitFormText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnExitText() {
        exitFormText.addTextChangedListener(textWatcher)
    }

    override fun onBackPressed() {
        if (btSubmitExitForm.isEnabled) {
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

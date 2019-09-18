package com.simprints.id.activities.faceexitform

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.faceexitform.result.FaceExitFormResult
import com.simprints.id.activities.faceexitform.result.FaceExitFormResult.Companion.FACE_EXIT_FORM_BUNDLE_KEY
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.exitform.FaceExitFormReason.*
import com.simprints.id.tools.extensions.showToast
import com.simprints.id.tools.textWatcherOnChange
import kotlinx.android.synthetic.main.activity_face_exit_form.*
import org.jetbrains.anko.inputMethodManager
import javax.inject.Inject

class FaceExitFormActivity : AppCompatActivity() {

    private var faceExitFormReason = OTHER

    @Inject lateinit var crashReportManager: CrashReportManager

    private val textWatcher = textWatcherOnChange {
        handleTextChangedInExitForm(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_exit_form)

        injectDependencies()

        setRadioGroupListener()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setRadioGroupListener() {
        faceExitFormRadioGroup.setOnCheckedChangeListener { _, optionIdentifier ->
            faceExitFormText.removeTextChangedListener(textWatcher)
            enableSubmitButton()
            enableFaceExitFormText()
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
        faceBtSubmitExitForm.isEnabled = true
    }

    private fun disableSubmitButton() {
        faceBtSubmitExitForm.isEnabled = false
    }

    private fun enableFaceExitFormText() {
        faceExitFormText.isEnabled = true
    }

    private fun handleRadioOptionIdentifierClick(optionIdentifier: Int) {
        when (optionIdentifier) {
            R.id.faceRbReligiousConcerns -> {
                faceExitFormReason = REFUSED_RELIGION
                logRadioOptionForCrashReport("Religious Concerns")
            }
            R.id.faceRbDataConcerns -> {
                faceExitFormReason = REFUSED_DATA_CONCERNS
                logRadioOptionForCrashReport("Data Concerns")
            }
            R.id.faceRbPersonNotPresent -> {
                faceExitFormReason = REFUSED_NOT_PRESENT
                logRadioOptionForCrashReport("Person not present")
            }
            R.id.faceRbTooYoung -> {
                faceExitFormReason = REFUSED_YOUNG
                logRadioOptionForCrashReport("Too young")
            }
            R.id.faceRbDoesNotHavePermission -> {
                faceExitFormReason = REFUSED_PERMISSION
                logRadioOptionForCrashReport("Does not have permission")
            }
            R.id.faceRbAppNotWorking -> {
                faceExitFormReason = SCANNER_NOT_WORKING
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("App not working")
            }
            R.id.faceRbOther -> {
                faceExitFormReason = OTHER
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("Other")
            }
        }
        //STOPSHIP: Remove on actual implementation
        faceRbReligiousConcerns.isChecked = true
    }

    private fun getExitFormText() = faceExitFormText.text.toString()

    private fun setFocusOnExitReasonAndDisableSubmit() {
        faceBtSubmitExitForm.isEnabled = false
        faceExitFormText.requestFocus()
        setTextChangeListenerOnExitText()
        inputMethodManager.showSoftInput(faceExitFormText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnExitText() {
        faceExitFormText.addTextChangedListener(textWatcher)
    }

    fun handleGoBackClick(@Suppress("UNUSED_PARAMETER")view: View) {
        setResultAndFinish(FaceExitFormResult.FACE_EXIT_FORM_RESULT_CODE_GO_BACK, FaceExitFormResult.Action.GO_BACK)
    }

    fun handleSubmitClick(@Suppress("UNUSED_PARAMETER")view: View) {
        setResultAndFinish(FaceExitFormResult.FACE_EXIT_FORM_RESULT_CODE_SUBMIT, FaceExitFormResult.Action.SUBMIT)
    }

    private fun setResultAndFinish(resultCode: Int, exitFormAction: FaceExitFormResult.Action) {
        setResult(resultCode, getIntentForAction(exitFormAction))
        finish()
    }

    private fun getIntentForAction(exitFormAction: FaceExitFormResult.Action) = Intent().apply {
        putExtra(FACE_EXIT_FORM_BUNDLE_KEY, buildExitFormResult(exitFormAction))
    }

    private fun buildExitFormResult(exitFormAction: FaceExitFormResult.Action) =
        FaceExitFormResult(exitFormAction, FaceExitFormResult.Answer(faceExitFormReason, getExitFormText()))


    override fun onBackPressed() {
        if (faceBtSubmitExitForm.isEnabled) {
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

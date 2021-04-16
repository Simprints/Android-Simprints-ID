package com.simprints.id.activities.faceexitform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.faceexitform.result.FaceExitFormActivityResult
import com.simprints.id.activities.faceexitform.result.FaceExitFormActivityResult.Action
import com.simprints.id.activities.faceexitform.result.FaceExitFormActivityResult.Answer
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.exitform.FaceExitFormReason.*
import com.simprints.id.databinding.ActivityFaceExitFormBinding
import com.simprints.id.exitformhandler.ExitFormResult.Companion.EXIT_FORM_BUNDLE_KEY
import com.simprints.id.tools.extensions.showToast
import com.simprints.id.tools.textWatcherOnChange
import com.simprints.id.tools.time.TimeHelper
import org.jetbrains.anko.inputMethodManager
import org.jetbrains.anko.sdk27.coroutines.onLayoutChange
import javax.inject.Inject

class FaceExitFormActivity : BaseSplitActivity() {

    private lateinit var viewModel: FaceExitFormViewModel
    private val binding by viewBinding(ActivityFaceExitFormBinding::inflate)

    @Inject
    lateinit var timeHelper: TimeHelper
    @Inject
    lateinit var crashReportManager: CrashReportManager
    @Inject
    lateinit var faceExitFormViewModelFactory: FaceExitFormViewModelFactory

    private var exitFormStartTime: Long = 0
    private var faceExitFormReason = OTHER

    private val textWatcher = textWatcherOnChange {
        handleTextChangedInExitForm(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        injectDependencies()

        setTextInLayout()

        viewModel = ViewModelProvider(this, faceExitFormViewModelFactory).get(FaceExitFormViewModel::class.java)
        exitFormStartTime = timeHelper.now()

        setRadioGroupListener()
        setLayoutChangeListener()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setTextInLayout() {
        binding.apply {
            whySkipFaceText.text = getString(R.string.why_did_you_skip_face_capture)
            faceRbReligiousConcerns.text = getString(R.string.refusal_religious_concerns)
            faceRbDataConcerns.text = getString(R.string.refusal_data_concerns)
            faceRbDoesNotHavePermission.text = getString(R.string.refusal_does_not_have_permission)
            faceRbAppNotWorking.text = getString(R.string.refusal_app_not_working)
            faceRbPersonNotPresent.text = getString(R.string.refusal_person_not_present)
            faceRbTooYoung.text = getString(R.string.refusal_too_young)
            faceRbOther.text = getString(R.string.refusal_other)
            faceExitFormText.hint = getString(R.string.hint_other_reason)
            faceBtSubmitExitForm.text = getString(R.string.button_submit)
            faceBtGoBack.text = getString(R.string.exit_form_capture_face)

        }
    }

    private fun setRadioGroupListener() {
        binding.faceExitFormRadioGroup.setOnCheckedChangeListener { _, optionIdentifier ->
            binding.faceExitFormText.removeTextChangedListener(textWatcher)
            enableSubmitButton()
            enableFaceExitFormText()
            handleRadioOptionIdentifierClick(optionIdentifier)
        }
    }

    //Changes in the layout occur when the keyboard shows up
    private fun setLayoutChangeListener() {
        binding.faceExitFormScrollView.onLayoutChange { _, _, _, _, _, _, _, _, _ ->
            binding.faceExitFormScrollView.fullScroll(View.FOCUS_DOWN)
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
        binding.faceBtSubmitExitForm.isEnabled = true
    }

    private fun disableSubmitButton() {
        binding.faceBtSubmitExitForm.isEnabled = false
    }

    private fun enableFaceExitFormText() {
        binding.faceExitFormText.isEnabled = true
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
                faceExitFormReason = APP_NOT_WORKING
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("App not working")
            }
            R.id.faceRbOther -> {
                faceExitFormReason = OTHER
                setFocusOnExitReasonAndDisableSubmit()
                logRadioOptionForCrashReport("Other")
            }
        }
    }

    private fun getExitFormText() = binding.faceExitFormText.text.toString()

    private fun setFocusOnExitReasonAndDisableSubmit() {
        binding.faceBtSubmitExitForm.isEnabled = false
        binding.faceExitFormText.requestFocus()
        setTextChangeListenerOnExitText()
        inputMethodManager.showSoftInput(binding.faceExitFormText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setTextChangeListenerOnExitText() {
        binding.faceExitFormText.addTextChangedListener(textWatcher)
    }

    fun handleGoBackClick(@Suppress("UNUSED_PARAMETER") view: View) {
        setResultAndFinish(Action.GO_BACK)
    }

    fun handleSubmitClick(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModel.addExitFormEvent(exitFormStartTime, timeHelper.now(), getExitFormText(), faceExitFormReason)
        setResultAndFinish(Action.SUBMIT)
    }

    private fun setResultAndFinish(exitFormActivityAction: Action) {
        setResult(Activity.RESULT_OK, getIntentForAction(exitFormActivityAction))
        finish()
    }

    private fun getIntentForAction(exitFormActivityAction: Action) = Intent().apply {
        putExtra(EXIT_FORM_BUNDLE_KEY, buildExitFormResult(exitFormActivityAction))
    }

    private fun buildExitFormResult(exitFormActivityAction: Action) =
        FaceExitFormActivityResult(exitFormActivityAction, Answer(faceExitFormReason, getExitFormText()))


    override fun onBackPressed() {
        if (binding.faceBtSubmitExitForm.isEnabled) {
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

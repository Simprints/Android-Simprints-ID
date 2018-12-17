package com.simprints.id.activities.refusal

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.db.remote.enums.REFUSAL_FORM_REASON
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.libsimprints.Constants
import kotlinx.android.synthetic.main.activity_refusal.*
import org.jetbrains.anko.sdk27.coroutines.onLayoutChange

class RefusalActivity : AppCompatActivity(), RefusalContract.View {

    override lateinit var viewPresenter: RefusalContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = (application as Application).component
        setContentView(R.layout.activity_refusal)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewPresenter = RefusalPresenter(this, component)

        setButtonClickListeners()
        setTextChangeListenerToRefusalText()
        setLayoutChangeListeners()
        setRadioGroupListener()
    }

    private fun setButtonClickListeners() {
        btSubmitRefusalForm.setOnClickListener { viewPresenter.handleSubmitButtonClick(getRefusalText()) }
        btScanFingerprints.setOnClickListener { viewPresenter.handleScanFingerprintsClick() }
    }

    private fun setTextChangeListenerToRefusalText() {
        refusalText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewPresenter.handleChangesInRefusalText(getRefusalText())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
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
            viewPresenter.handleRadioOptionClicked(optionIdentifier)
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

    override fun enableRefusalText() {
        refusalText.isEnabled = true
    }

    override fun doLaunchAlert(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }

    override fun setResultAndFinish(activityResult: Int, reason: REFUSAL_FORM_REASON?) {
        setResult(activityResult, getIntentForResultData(reason))
        finish()
    }

    private fun getIntentForResultData(reason: REFUSAL_FORM_REASON?) =
        Intent().putExtra(Constants.SIMPRINTS_REFUSAL_FORM, reason)

    private fun getRefusalText() = refusalText.text.toString()
}

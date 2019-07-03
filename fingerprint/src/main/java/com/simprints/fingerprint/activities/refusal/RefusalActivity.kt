package com.simprints.fingerprint.activities.refusal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.refusal.result.RefusalActResult
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.tools.extensions.logActivityCreated
import com.simprints.fingerprint.tools.extensions.logActivityDestroyed
import com.simprints.id.Application
import kotlinx.android.synthetic.main.activity_refusal.*
import org.jetbrains.anko.sdk27.coroutines.onLayoutChange

class RefusalActivity : AppCompatActivity(), RefusalContract.View {

    override lateinit var viewPresenter: RefusalContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logActivityCreated()
        val component = FingerprintComponentBuilder.getComponent(application as Application)
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

    override fun setResultAndFinish(activityResult: Int, refusalResult: RefusalActResult) {
        setResult(activityResult, getIntentForResultData(refusalResult))
        finish()
    }

    private fun getIntentForResultData(refusalResult: RefusalActResult) =
        Intent().putExtra(
            RefusalActResult.BUNDLE_KEY,
            refusalResult)

    private fun getRefusalText() = refusalText.text.toString()

    override fun onDestroy() {
        super.onDestroy()
        logActivityDestroyed()
    }
}

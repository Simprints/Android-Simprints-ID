package com.simprints.clientapi.activities.errors

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.simprints.clientapi.R
import com.simprints.clientapi.activities.errors.request.AlertActRequest
import com.simprints.clientapi.activities.errors.response.AlertActResponse
import com.simprints.core.tools.activity.BaseSplitActivity
import kotlinx.android.synthetic.main.activity_error.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.backgroundColor
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class ErrorActivity : BaseSplitActivity(), ErrorContract.View {

    override val presenter: ErrorContract.Presenter by inject { parametersOf(this) }

    private lateinit var clientApiAlertType: ClientApiAlert

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        setTextInLayout()

        clientApiAlertType = intent
            .extras?.getParcelable<AlertActRequest>(AlertActRequest.BUNDLE_KEY)?.clientApiAlert
            ?: throw Throwable("No AlertActRequest found")

        textView_close_button.setOnClickListener { presenter.handleCloseOrBackClick() }

        lifecycleScope.launch {
            presenter.start(clientApiAlertType)
        }
    }

    private fun setTextInLayout() {
        alert_image.contentDescription = getString(R.string.main_error_graphic)
        textView_close_button.text = getString(R.string.close)
    }

    override fun closeActivity() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResponse.BUNDLE_KEY, AlertActResponse(clientApiAlertType))
        })

        finish()
    }

    override fun setErrorTitleText(title: String) {
        textView_error_title.text = title
    }

    override fun setErrorMessageText(message: String) {
        textView_message.text = message
    }

    override fun setBackgroundColour(colour: Int) {
        alertLayout.backgroundColor = colour
        textView_close_button.backgroundColor = colour
    }

    override fun setErrorHintVisible(isHintVisible: Boolean) {
        imageView_error_hint.visibility = if (isHintVisible) VISIBLE else GONE
    }

    override fun getStringFromResources(res: Int): String = getString(res)

    override fun getColourFromResources(colourId: Int): Int = ContextCompat.getColor(this, colourId)

    override fun onBackPressed() {
        presenter.handleCloseOrBackClick()
    }

}

package com.simprints.clientapi.activities.errors

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.R
import com.simprints.clientapi.activities.errors.request.AlertActRequest
import com.simprints.clientapi.activities.errors.response.AlertActResponse
import com.simprints.id.tools.AndroidResourcesHelper
import kotlinx.android.synthetic.main.activity_error.*
import org.jetbrains.anko.backgroundColor
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class ErrorActivity : AppCompatActivity(), ErrorContract.View {

    override val presenter: ErrorContract.Presenter by inject { parametersOf(this) }
    private val androidResHelper: AndroidResourcesHelper by inject()

    private lateinit var clientApiAlertType: ClientApiAlert

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        setTextInLayout()

        clientApiAlertType = intent
            .extras?.getParcelable<AlertActRequest>(AlertActRequest.BUNDLE_KEY)?.clientApiAlert
            ?: ClientApiAlert.INVALID_CLIENT_REQUEST

        presenter.start(clientApiAlertType)

        textView_close_button.setOnClickListener { presenter.handleCloseOrBackClick() }
    }

    private fun setTextInLayout() {
        with(androidResHelper) {
            alert_image.contentDescription = getString(R.string.main_error_graphic)
            textView_close_button.text = getString(R.string.close)
        }
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

    override fun getStringFromResources(res: Int): String = androidResHelper.getString(res)

    override fun getColourFromResources(colourId: Int): Int = androidResHelper.getColour(colourId)

    override fun onBackPressed() {
        presenter.handleCloseOrBackClick()
    }

}

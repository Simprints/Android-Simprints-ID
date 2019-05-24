package com.simprints.clientapi.activities.errors

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.R
import com.simprints.clientapi.activities.errors.di.ErrorActivityComponentInjector
import com.simprints.clientapi.activities.errors.request.AlertActRequest
import com.simprints.clientapi.activities.errors.response.AlertActResponse
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import kotlinx.android.synthetic.main.activity_error.*
import javax.inject.Inject

class ErrorActivity : AppCompatActivity(), ErrorContract.View {

    @Inject override lateinit var presenter: ErrorContract.Presenter
    @Inject lateinit var clientApiSessionEventsManager: ClientApiSessionEventsManager

    private lateinit var clientApiAlertType: ClientApiAlert

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)


        clientApiAlertType = intent.extras?.getParcelable<AlertActRequest>(AlertActRequest.BUNDLE_KEY)?.clientApiAlert
            ?: ClientApiAlert.INVALID_CLIENT_REQUEST

        presenter.start(clientApiAlertType)

        textView_close_button.setOnClickListener { presenter.handleCloseClick() }
    }

    override fun closeActivity() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResponse.BUNDLE_KEY, AlertActResponse(clientApiAlertType))
        })

        finish()
    }

    override fun setErrorMessageText(message: String) {
        textView_message.text = message
    }

    override fun getStringFromResources(res: Int): String = getString(res)

    override fun onDestroy() {
        super.onDestroy()
        ErrorActivityComponentInjector.setComponent(null)
    }
}

package com.simprints.clientapi.activities.errors

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.R
import com.simprints.clientapi.activities.errors.request.AlertActRequest
import com.simprints.clientapi.activities.errors.response.AlertActResponse
import com.simprints.clientapi.di.koinModule
import kotlinx.android.synthetic.main.activity_error.*
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.parameter.parametersOf


class ErrorActivity : AppCompatActivity(), ErrorContract.View {

    // TODO: if we go with koin replace base lateinit var with val
    override lateinit var presenter: ErrorContract.Presenter
    private val pres: ErrorContract.Presenter by inject { parametersOf(this) }

    private lateinit var clientApiAlertType: ClientApiAlert

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(koinModule)
        setContentView(R.layout.activity_error)

        presenter = pres

        clientApiAlertType = intent
            .extras?.getParcelable<AlertActRequest>(AlertActRequest.BUNDLE_KEY)?.clientApiAlert
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
        // TODO: If we use scope properly we don't need this
        unloadKoinModules(koinModule)
    }

}

package com.simprints.clientapi.activities.errors

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.R
import kotlinx.android.synthetic.main.activity_error.*

class ErrorActivity : AppCompatActivity(), ErrorContract.View {

    companion object {
        const val MESSAGE_KEY = "messageKey"
    }

    override lateinit var presenter: ErrorContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        presenter = ErrorPresenter(this, intent.getStringExtra(MESSAGE_KEY)).apply { start() }
        textView_close_button.setOnClickListener { presenter.handleCloseClick() }
    }

    override fun closeActivity() {
        setResult(Activity.RESULT_CANCELED)
        finishAffinity()
    }

    override fun setErrorMessageText(message: String) {
        textView_message.text = getString(R.string.configuration_error_message, message)
    }

}

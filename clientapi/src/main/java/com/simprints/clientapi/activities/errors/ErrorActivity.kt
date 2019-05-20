package com.simprints.clientapi.activities.errors

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.R
import com.simprints.clientapi.activities.errors.di.ErrorActivityComponentInjector
import kotlinx.android.synthetic.main.activity_error.*
import javax.inject.Inject

class ErrorActivity : AppCompatActivity(), ErrorContract.View {

    companion object {
        const val MESSAGE_KEY = "messageKey"
    }

    @Inject override lateinit var presenter: ErrorContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        ErrorActivityComponentInjector.inject(this)

        presenter.start()

        textView_close_button.setOnClickListener { presenter.handleCloseClick() }
    }

    override fun closeActivity() {
        setResult(Activity.RESULT_CANCELED)
        finishAffinity()
    }

    override fun setErrorMessageText(message: String) {
        textView_message.text = getString(R.string.configuration_error_message, message)
    }

    override fun getErrorMessage() = intent.getStringExtra(MESSAGE_KEY) ?: ""

    override fun onDestroy() {
        super.onDestroy()
        ErrorActivityComponentInjector.setComponent(null)
    }
}

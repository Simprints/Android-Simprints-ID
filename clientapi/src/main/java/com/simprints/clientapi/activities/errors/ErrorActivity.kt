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
import com.simprints.clientapi.databinding.ActivityErrorBinding
import com.simprints.core.tools.activity.BaseSplitActivity
import kotlinx.coroutines.launch
import org.jetbrains.anko.backgroundColor
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class ErrorActivity : BaseSplitActivity(), ErrorContract.View {

    override val presenter: ErrorContract.Presenter by inject { parametersOf(this) }

    private lateinit var clientApiAlertType: ClientApiAlert
    private lateinit var binding: ActivityErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTextInLayout()

        clientApiAlertType = intent
            .extras?.getParcelable<AlertActRequest>(AlertActRequest.BUNDLE_KEY)?.clientApiAlert
            ?: throw Throwable("No AlertActRequest found")

        binding.textViewCloseButton.setOnClickListener { presenter.handleCloseOrBackClick() }

        lifecycleScope.launch {
            presenter.start(clientApiAlertType)
        }
    }

    private fun setTextInLayout() {
        binding.alertImage.contentDescription = getString(R.string.main_error_graphic)
        binding.textViewCloseButton.text = getString(R.string.close)
    }

    override fun closeActivity() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResponse.BUNDLE_KEY, AlertActResponse(clientApiAlertType))
        })

        finish()
    }

    override fun setErrorTitleText(title: String) {
        binding.textViewErrorTitle.text = title
    }

    override fun setErrorMessageText(message: String) {
        binding.textViewMessage.text = message
    }

    override fun setBackgroundColour(colour: Int) {
        binding.alertLayout.backgroundColor = colour
        binding.textViewCloseButton.backgroundColor = colour
    }

    override fun setErrorHintVisible(isHintVisible: Boolean) {
        binding.imageViewErrorHint.visibility = if (isHintVisible) VISIBLE else GONE
    }

    override fun getStringFromResources(res: Int): String = getString(res)

    override fun getColourFromResources(colourId: Int): Int = ContextCompat.getColor(this, colourId)

    override fun onBackPressed() {
        presenter.handleCloseOrBackClick()
    }

}

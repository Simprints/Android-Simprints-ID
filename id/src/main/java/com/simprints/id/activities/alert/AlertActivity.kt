package com.simprints.id.activities.alert

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.request.AlertActRequest
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertType
import kotlinx.android.synthetic.main.activity_alert.*

class AlertActivity : AppCompatActivity(), AlertContract.View {

    override lateinit var viewPresenter: AlertContract.Presenter
    private lateinit var alertTypeType: AlertType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val app = application as Application

        alertTypeType = intent.extras?.let {
            it.get(AlertActRequest.BUNDLE_KEY) as AlertActRequest
        }?.alertType ?: AlertType.UNEXPECTED_ERROR

        viewPresenter = AlertPresenter(this, app.component, alertTypeType)
        viewPresenter.start()
    }

    override fun getColorForColorRes(@ColorRes colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, null)
    override fun setLayoutBackgroundColor(@ColorInt color: Int) = alertLayout.setBackgroundColor(color)
    override fun setLeftButtonBackgroundColor(@ColorInt color: Int) = alertLeftButton.setBackgroundColor(color)
    override fun setRightButtonBackgroundColor(@ColorInt color: Int) = alertRightButton.setBackgroundColor(color)
    override fun setAlertTitleWithStringRes(@StringRes stringRes: Int) = alertTitle.setText(stringRes)
    override fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int) = alertImage.setImageResource(drawableId)
    override fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?) {
        if (alertHintDrawableId != null) {
            hintGraphic.setImageResource(alertHintDrawableId)
        } else {
            hintGraphic.visibility = View.GONE
        }
    }

    override fun setAlertMessageWithStringRes(@StringRes stringRes: Int) = message.setText(stringRes)

    override fun initLeftButton(leftButtonAction: AlertActivityViewModel.ButtonAction) {
        if (leftButtonAction !is AlertActivityViewModel.ButtonAction.None) {
            alertLeftButton.setText(leftButtonAction.buttonText)
            alertLeftButton.setOnClickListener { viewPresenter.handleButtonClick(leftButtonAction) }
        } else {
            alertLeftButton.visibility = View.GONE
        }
    }

    override fun initRightButton(rightButtonAction: AlertActivityViewModel.ButtonAction) {
        if (rightButtonAction !is AlertActivityViewModel.ButtonAction.None) {
            alertRightButton.setText(rightButtonAction.buttonText)
            alertRightButton.setOnClickListener { viewPresenter.handleButtonClick(rightButtonAction) }
        } else {
            alertRightButton.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        viewPresenter.handleBackButton()
        super.onBackPressed()
    }

    override fun closeActivityAfterCloseButton() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResponse.BUNDLE_KEY, AlertActResponse(alertTypeType, AlertActResponse.ButtonAction.CLOSE))
        })

        finish()
    }
}

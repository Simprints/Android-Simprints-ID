package com.simprints.fingerprint.activities.alert

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
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.request.AlertActRequest
import com.simprints.fingerprint.activities.alert.response.AlertActResult.CloseButtonAction.*
import com.simprints.fingerprint.activities.alert.response.AlertActResult
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.id.Application
import kotlinx.android.synthetic.main.activity_fingerprint_alert.*
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*

class AlertActivity : AppCompatActivity(), AlertContract.View {

    override lateinit var viewPresenter: AlertContract.Presenter
    private lateinit var alertType: FingerprintAlert

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint_alert)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        alertType = intent.extras?.getParcelable<AlertActRequest>(AlertActRequest.BUNDLE_KEY)?.alert
            ?: UNEXPECTED_ERROR

        val component = FingerprintComponentBuilder.getComponent(application as Application)
        viewPresenter = AlertPresenter(this, component, alertType)
        viewPresenter.start()
    }

    override fun getColorForColorRes(@ColorRes colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, null)
    override fun setLayoutBackgroundColor(@ColorInt color: Int) = alertLayout.setBackgroundColor(color)
    override fun setLeftButtonBackgroundColor(@ColorInt color: Int) = left_button.setBackgroundColor(color)
    override fun setRightButtonBackgroundColor(@ColorInt color: Int) = right_button.setBackgroundColor(color)
    override fun setAlertTitleWithStringRes(@StringRes stringRes: Int) = alert_title.setText(stringRes)
    override fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int) = alert_image.setImageResource(drawableId)
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
            left_button.setText(leftButtonAction.buttonText)
            left_button.setOnClickListener { viewPresenter.handleButtonClick(leftButtonAction) }
        } else {
            left_button.visibility = View.GONE
        }
    }

    override fun initRightButton(rightButtonAction: AlertActivityViewModel.ButtonAction) {
        if (rightButtonAction !is AlertActivityViewModel.ButtonAction.None) {
            right_button.setText(rightButtonAction.buttonText)
            right_button.setOnClickListener { viewPresenter.handleButtonClick(rightButtonAction) }
        } else {
            right_button.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResult.BUNDLE_KEY, AlertActResult(alertType, handleBackButton(alertType)))
        })

        finish()
        super.onBackPressed()
    }

    private fun handleBackButton(alertType: FingerprintAlert): AlertActResult.CloseButtonAction =
        when(alertType) {
            GUID_NOT_FOUND_OFFLINE,
            BLUETOOTH_NOT_ENABLED,
            NOT_PAIRED,
            MULTIPLE_PAIRED_SCANNERS,
            DISCONNECTED -> TRY_AGAIN
            else -> CLOSE
        }

    override fun openBluetoothSettings() {
        val intent = Intent()
        intent.action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
        startActivity(intent)
    }

    override fun openWifiSettings() {
        val intent = Intent()
        intent.action = android.provider.Settings.ACTION_WIFI_SETTINGS
        startActivity(intent)
    }

    override fun closeActivityAfterTryAgainButton() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResult.BUNDLE_KEY, AlertActResult(alertType, TRY_AGAIN))
        })

        finish()
    }

    override fun closeActivityAfterCloseButton() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResult.BUNDLE_KEY, AlertActResult(alertType, CLOSE))
        })

        finish()
    }
}

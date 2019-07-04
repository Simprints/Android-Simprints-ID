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
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.alert.request.AlertActRequest
import com.simprints.fingerprint.activities.alert.response.AlertActResult
import com.simprints.fingerprint.activities.alert.response.AlertActResult.CloseButtonAction.*
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.id.Application
import kotlinx.android.synthetic.main.activity_fingerprint_alert.*

class AlertActivity : AppCompatActivity(), AlertContract.View {

    override lateinit var viewPresenter: AlertContract.Presenter
    private lateinit var alertType: FingerprintAlert

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        alertType = intent.extras?.getParcelable<AlertActRequest>(AlertActRequest.BUNDLE_KEY)?.alert
            ?: UNEXPECTED_ERROR

        if(isBluetoothAlert(alertType)) {
           setContentView(R.layout.activity_fingerprint_bluetooth_alert)
        } else {
            setContentView(R.layout.activity_fingerprint_alert)
        }

        val component = FingerprintComponentBuilder.getComponent(application as Application)
        viewPresenter = AlertPresenter(this, component, alertType)
        viewPresenter.start()
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.handleOnResume()

    }

    private fun isBluetoothAlert(alertType: FingerprintAlert) =
        (alertType == DISCONNECTED || alertType == NOT_PAIRED)

    override fun getColorForColorRes(@ColorRes colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, null)
    override fun setLayoutBackgroundColor(@ColorInt color: Int) = alertLayout.setBackgroundColor(color)
    override fun setLeftButtonBackgroundColor(@ColorInt color: Int) { alertLeftButton?.setBackgroundColor(color) }
    override fun setRightButtonBackgroundColor(@ColorInt color: Int) { alertRightButton?.setBackgroundColor(color) }
    override fun setAlertTitleWithStringRes(@StringRes stringRes: Int) = alert_title.setText(stringRes)
    override fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int) = alert_image.setImageResource(drawableId)
    override fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?) {
        if (alertHintDrawableId != null) {
            hintGraphic?.setImageResource(alertHintDrawableId)
        } else {
            hintGraphic?.visibility = View.GONE
        }
    }

    override fun setAlertMessageWithStringRes(@StringRes stringRes: Int) = message.setText(stringRes)

    override fun initLeftButton(leftButtonAction: AlertActivityViewModel.ButtonAction) {
        if (leftButtonAction !is AlertActivityViewModel.ButtonAction.None) {
            alertLeftButton.visibility = View.VISIBLE
            alertLeftButton.setText(leftButtonAction.buttonText)
            alertLeftButton.setOnClickListener { viewPresenter.handleButtonClick(leftButtonAction) }
        }
    }

    override fun initRightButton(rightButtonAction: AlertActivityViewModel.ButtonAction) {
        if (rightButtonAction !is AlertActivityViewModel.ButtonAction.None) {
            alertLeftButton.visibility = View.VISIBLE
            alertRightButton.setText(rightButtonAction.buttonText)
            alertRightButton.setOnClickListener { viewPresenter.handleButtonClick(rightButtonAction) }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResult.BUNDLE_KEY, AlertActResult(alertType, BACK))
        })

        finish()
        super.onBackPressed()
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

    override fun setTryAgainResultAndFinish() {
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

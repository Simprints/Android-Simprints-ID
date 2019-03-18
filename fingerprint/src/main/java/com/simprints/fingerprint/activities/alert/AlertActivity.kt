package com.simprints.fingerprint.activities.alert

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
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.fingerprint.di.FingerprintsComponentBuilder
import com.simprints.id.Application
import com.simprints.id.activities.IntentKeys
import kotlinx.android.synthetic.main.activity_fingerprint_alert.*

class AlertActivity : AppCompatActivity(), AlertContract.View {

   override lateinit var viewPresenter: AlertContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint_alert)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val alertType = intent.extras?.let {
            it.get(IntentKeys.alertActivityAlertTypeKey) as FingerprintAlert
        } ?: FingerprintAlert.UNEXPECTED_ERROR

        val component = FingerprintsComponentBuilder.getComponent(application as Application)
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

    override fun initLeftButton(leftButtonAction: FingerprintAlert.ButtonAction) {
        if (leftButtonAction !is FingerprintAlert.ButtonAction.None) {
            left_button.setText(leftButtonAction.buttonText)
            left_button.setOnClickListener { viewPresenter.handleButtonClick(leftButtonAction) }
        } else {
            left_button.visibility = View.GONE
        }
    }

    override fun initRightButton(rightButtonAction: FingerprintAlert.ButtonAction) {
        if (rightButtonAction !is FingerprintAlert.ButtonAction.None) {
            right_button.setText(rightButtonAction.buttonText)
            right_button.setOnClickListener { viewPresenter.handleButtonClick(rightButtonAction) }
        } else {
            right_button.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        viewPresenter.handleBackButton()
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

    override fun closeActivity() = finish()
    override fun closeAllActivities() = finishAffinity()
}

package com.simprints.id.activities.alert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.domain.alert.Alert
import kotlinx.android.synthetic.main.activity_alert.*


class AlertActivity : AppCompatActivity(), AlertContract.View {

    override lateinit var viewPresenter: AlertContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val app = application as Application

        val alertType = intent.extras?.let {
            it.get(IntentKeys.alertActivityAlertTypeKey) as Alert
        } ?: Alert.UNEXPECTED_ERROR

        viewPresenter = AlertPresenter(this, app.component, alertType)
        viewPresenter.start()
    }

    override fun getColorForColorRes(colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, null)
    override fun setLayoutBackgroundColor(color: Int) = alertLayout.setBackgroundColor(color)
    override fun setLeftButtonBackgroundColor(color: Int) = left_button.setBackgroundColor(color)
    override fun setRightButtonBackgroundColor(color: Int) = right_button.setBackgroundColor(color)
    override fun setAlertTitleWithStringRes(stringRes: Int) = alert_title.setText(stringRes)
    override fun setAlertImageWithDrawableId(drawableId: Int) = alert_image.setImageResource(drawableId)
    override fun setAlertHintImageWithDrawableId(alertHintDrawableId: Int?) {
        if (alertHintDrawableId != null) {
            hintGraphic.setImageResource(alertHintDrawableId)
        } else {
            hintGraphic.visibility = View.GONE
        }
    }

    override fun setAlertMessageWithStringRes(stringRes: Int) = message.setText(stringRes)

    override fun initLeftButton(leftButtonAction: Alert.ButtonAction) {
        if (leftButtonAction !is Alert.ButtonAction.None) {
            left_button.setText(leftButtonAction.buttonText)
            left_button.setOnClickListener { viewPresenter.handleButtonClick(leftButtonAction) }
        } else {
            left_button.visibility = View.GONE
        }
    }

    override fun initRightButton(rightButtonAction: Alert.ButtonAction) {
        if (rightButtonAction !is Alert.ButtonAction.None) {
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

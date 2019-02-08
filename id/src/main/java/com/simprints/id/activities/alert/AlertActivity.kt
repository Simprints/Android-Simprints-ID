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
import com.simprints.id.domain.ALERT_TYPE
import kotlinx.android.synthetic.main.activity_alert.*

class AlertActivity : AppCompatActivity(), AlertContract.View {

    override lateinit var viewPresenter: AlertContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val app = application as Application

        val alertType = intent.extras?.let {
            it.get(IntentKeys.alertActivityAlertTypeKey) as ALERT_TYPE
        } ?: ALERT_TYPE.UNEXPECTED_ERROR

        viewPresenter = AlertPresenter(this, app.component, alertType)
        viewPresenter.start()
    }

    override fun getColorForColorRes(colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, null)
    override fun setLayoutBackgroundColor(color: Int) = alertLayout.setBackgroundColor(color)
    override fun setLeftButtonBackgroundColor(color: Int) = left_button.setBackgroundColor(color)
    override fun setRightButtonBackgroundColor(color: Int) = right_button.setBackgroundColor(color)
    override fun setAlertTitleWithStringRes(stringRes: Int) = alert_title.setText(stringRes)
    override fun setAlertImageWithDrawableId(drawableId: Int) = alert_image.setImageResource(drawableId)
    override fun setAlertHintImageWithDrawableId(alertHintDrawableId: Int) {
        if (alertHintDrawableId != -1) {
            hintGraphic.setImageResource(alertHintDrawableId)
        } else {
            hintGraphic.visibility = View.GONE
        }
    }

    override fun hideLeftButton() {
        left_button.visibility = View.GONE
    }

    override fun setAlertMessageWithStringRes(stringRes: Int) = message.setText(stringRes)

    override fun initLeftButton(alertType: ALERT_TYPE) {
        if (alertType.isLeftButtonActive) {
            left_button.setText(alertType.alertLeftButtonTextId)
            left_button.setOnClickListener {
                viewPresenter.handleLeftButtonClick()
            }
        }
    }

    override fun initRightButton(alertType: ALERT_TYPE) {
        right_button.setText(alertType.alertRightButtonTextId)
        right_button.setOnClickListener {
            viewPresenter.handleRightButtonClick()
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

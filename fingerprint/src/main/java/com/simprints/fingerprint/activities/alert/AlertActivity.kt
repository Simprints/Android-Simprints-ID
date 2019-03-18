package com.simprints.fingerprint.activities.alert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.simprints.fingerprint.di.FingerprintsComponentBuilder
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.domain.alert.Alert

class AlertActivity : AppCompatActivity(), AlertContract.View {

    private val alertLayout by lazy { findViewById<LinearLayout>(R.id.alertLayout) }
    private val leftButton by lazy { findViewById<Button>(R.id.left_button) }
    private val rightButton by lazy { findViewById<Button>(R.id.right_button) }
    private val alertTitle by lazy { findViewById<TextView>(R.id.alert_title) }
    private val alertImage by lazy { findViewById<ImageView>(R.id.alert_image) }
    private val hintGraphic by lazy { findViewById<ImageView>(R.id.hintGraphic) }
    private val message by lazy { findViewById<TextView>(R.id.message) }

    override lateinit var viewPresenter: AlertContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val alertType = intent.extras?.let {
            it.get(IntentKeys.alertActivityAlertTypeKey) as Alert
        } ?: Alert.UNEXPECTED_ERROR

        val component = FingerprintsComponentBuilder.getComponent(application as Application)
        viewPresenter = AlertPresenter(this, component, alertType)
        viewPresenter.start()
    }

    override fun getColorForColorRes(@ColorRes colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, null)
    override fun setLayoutBackgroundColor(@ColorInt color: Int) = alertLayout.setBackgroundColor(color)
    override fun setLeftButtonBackgroundColor(@ColorInt color: Int) = leftButton.setBackgroundColor(color)
    override fun setRightButtonBackgroundColor(@ColorInt color: Int) = rightButton.setBackgroundColor(color)
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

    override fun initLeftButton(leftButtonAction: Alert.ButtonAction) {
        if (leftButtonAction !is Alert.ButtonAction.None) {
            leftButton.setText(leftButtonAction.buttonText)
            leftButton.setOnClickListener { viewPresenter.handleButtonClick(leftButtonAction) }
        } else {
            leftButton.visibility = View.GONE
        }
    }

    override fun initRightButton(rightButtonAction: Alert.ButtonAction) {
        if (rightButtonAction !is Alert.ButtonAction.None) {
            rightButton.setText(rightButtonAction.buttonText)
            rightButton.setOnClickListener { viewPresenter.handleButtonClick(rightButtonAction) }
        } else {
            rightButton.visibility = View.GONE
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

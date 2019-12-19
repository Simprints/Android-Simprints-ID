package com.simprints.fingerprint.activities.alert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.alert.request.AlertTaskRequest
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import kotlinx.android.synthetic.main.activity_fingerprint_alert.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class AlertActivity : FingerprintActivity(), AlertContract.View {

    val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()
    private lateinit var alertType: FingerprintAlert
    override val viewPresenter: AlertContract.Presenter by inject { parametersOf(this, alertType) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        alertType = intent.extras?.getParcelable<AlertTaskRequest>(AlertTaskRequest.BUNDLE_KEY)?.alert
            ?: UNEXPECTED_ERROR

        if (isNewBluetoothAlert(alertType)) {
           setContentView(R.layout.activity_fingerprint_bluetooth_alert)
        } else {
            setContentView(R.layout.activity_fingerprint_alert)
        }

        viewPresenter.start()
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.handleOnResume()
    }

    //This check will be removed in the next release as we will be switching to the 'activity_fingerprint_bluetooth_alert' for all alerts
    private fun isNewBluetoothAlert(alertType: FingerprintAlert) =
        (alertType == DISCONNECTED || alertType == NOT_PAIRED)

    override fun getColorForColorRes(@ColorRes colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, null)
    override fun setLayoutBackgroundColor(@ColorInt color: Int) = alertLayout.setBackgroundColor(color)
    override fun setLeftButtonBackgroundColor(@ColorInt color: Int) {
        alertLeftButton?.setBackgroundColor(color)
    }

    override fun setRightButtonBackgroundColor(@ColorInt color: Int) {
        alertRightButton?.setBackgroundColor(color)
    }

    override fun setAlertTitleWithStringRes(@StringRes stringRes: Int) {
        alertTitle.text = androidResourcesHelper.getString(stringRes)
    }

    override fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int) = alertImage.setImageResource(drawableId)
    override fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?) {
        if (alertHintDrawableId != null) {
            hintGraphic?.setImageResource(alertHintDrawableId)
        } else {
            hintGraphic?.visibility = View.GONE
        }
    }

    override fun setAlertMessageWithStringRes(@StringRes stringRes: Int) {
        message.text = androidResourcesHelper.getString(stringRes)
    }

    override fun initLeftButton(leftButtonAction: AlertActivityViewModel.ButtonAction) {
        if (leftButtonAction !is AlertActivityViewModel.ButtonAction.None) {
            alertLeftButton?.visibility = View.VISIBLE
            alertLeftButton?.text = androidResourcesHelper.getString(leftButtonAction.buttonText)
            alertLeftButton?.setOnClickListener { viewPresenter.handleButtonClick(leftButtonAction) }
        }
    }

    override fun initRightButton(rightButtonAction: AlertActivityViewModel.ButtonAction) {
        if (rightButtonAction !is AlertActivityViewModel.ButtonAction.None) {
            alertRightButton?.visibility = View.VISIBLE
            alertRightButton?.text = androidResourcesHelper.getString(rightButtonAction.buttonText)
            alertRightButton?.setOnClickListener { viewPresenter.handleButtonClick(rightButtonAction) }
        }
    }

    override fun onBackPressed() {
        viewPresenter.handleBackPressed()
    }

    override fun startRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java),
            RequestCode.REFUSAL.value)
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

    override fun finishWithAction(buttonAction: AlertTaskResult.CloseButtonAction) {
        val resultCode = when (buttonAction) {
            AlertTaskResult.CloseButtonAction.CLOSE,
            AlertTaskResult.CloseButtonAction.BACK -> ResultCode.ALERT
            AlertTaskResult.CloseButtonAction.TRY_AGAIN -> ResultCode.OK
        }

        setResultAndFinish(resultCode, Intent().apply {
            putExtra(AlertTaskResult.BUNDLE_KEY, AlertTaskResult(alertType, buttonAction))
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> {
                }
            }
        }
    }

    private fun setResultAndFinish(resultCode: ResultCode, data: Intent?) {
        setResult(resultCode.value, data)
        finish()
    }
}

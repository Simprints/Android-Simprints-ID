package com.simprints.fingerprint.activities.alert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.simprints.fingerprint.FingerprintModule
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.alert.request.AlertTaskRequest
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.fingerprint.databinding.ActivityFingerprintAlertBinding as AlertBinding
import com.simprints.fingerprint.databinding.ActivityFingerprintBluetoothAlertBinding as BluetoothAlertBinding

/**
 * This class represents the view for the [AlertContract], providing the user with the ability to
 * handle errors that occurred while processing a fingerprint request triggering functions like:
 * [openWifiSettings] and [finishWithAction].
 */
@AndroidEntryPoint
class AlertActivity : FingerprintActivity(), AlertContract.View {

    private lateinit var alertType: FingerprintAlert

    @Inject
    lateinit var presenterFactory: FingerprintModule.AlertPresenterFactory

    override val viewPresenter: AlertContract.Presenter by lazy {
        presenterFactory.create(
            this,
            alertType
        )
    }

    private lateinit var alertLeftButton: TextView
    private lateinit var alertLayout: LinearLayout
    private lateinit var alertTitle: TextView
    private lateinit var alertImage: ImageView
    private lateinit var message: TextView
    private var alertRightButton: TextView? = null
    private var hintGraphic: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        alertType =
            intent.extras?.getParcelable<AlertTaskRequest>(AlertTaskRequest.BUNDLE_KEY)?.alert
                ?: UNEXPECTED_ERROR

        if (isNewBluetoothAlert(alertType)) {
            BluetoothAlertBinding.inflate(layoutInflater).let {
                setContentView(it.root)
                setViewsWithBluetoothAlertBinding(it)
            }
        } else {
            AlertBinding.inflate(layoutInflater).let {
                setContentView(it.root)
                setViewsWithAlertBinding(it)
            }
        }

        viewPresenter.start()
    }

    private fun setViewsWithAlertBinding(binding: AlertBinding) {
        alertLayout = binding.alertLayout
        alertTitle = binding.alertTitle
        alertImage = binding.alertImage
        message = binding.message
        alertLeftButton = binding.alertLeftButton
        alertRightButton = binding.alertRightButton
        hintGraphic = binding.hintGraphic
    }

    private fun setViewsWithBluetoothAlertBinding(binding: BluetoothAlertBinding) {
        alertLayout = binding.alertLayout
        alertTitle = binding.alertTitle
        alertImage = binding.alertImage
        message = binding.message
        alertLeftButton = binding.alertLeftButton
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.handleOnResume()
    }

    //This check will be removed in the next release as we will be switching to the 'activity_fingerprint_bluetooth_alert' for all alerts
    private fun isNewBluetoothAlert(alertType: FingerprintAlert) =
        (alertType == DISCONNECTED || alertType == NOT_PAIRED)

    override fun getColorForColorRes(@ColorRes colorRes: Int) =
        ResourcesCompat.getColor(resources, colorRes, null)

    override fun setLayoutBackgroundColor(@ColorInt color: Int) =
        alertLayout.setBackgroundColor(color)

    override fun setLeftButtonBackgroundColor(@ColorInt color: Int) {
        alertLeftButton.setBackgroundColor(color)
    }

    override fun setRightButtonBackgroundColor(@ColorInt color: Int) {
        alertRightButton?.setBackgroundColor(color)
    }

    override fun setAlertTitleWithStringRes(@StringRes stringRes: Int) {
        alertTitle.text = getString(stringRes)
    }

    override fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int) =
        alertImage.setImageResource(drawableId)

    override fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?) {
        if (alertHintDrawableId != null) {
            hintGraphic?.setImageResource(alertHintDrawableId)
        } else {
            hintGraphic?.visibility = View.GONE
        }
    }

    override fun setAlertMessageWithStringRes(@StringRes stringRes: Int) {
        message.text = getString(stringRes)
    }

    override fun initLeftButton(leftButtonAction: AlertError.ButtonAction) {
        if (leftButtonAction !is AlertError.ButtonAction.None) {
            alertLeftButton.visibility = View.VISIBLE
            alertLeftButton.text = getString(leftButtonAction.buttonText)
            alertLeftButton.setOnClickListener { viewPresenter.handleButtonClick(leftButtonAction) }
        }
    }

    override fun initRightButton(rightButtonAction: AlertError.ButtonAction) {
        if (rightButtonAction !is AlertError.ButtonAction.None) {
            alertRightButton?.visibility = View.VISIBLE
            alertRightButton?.text = getString(rightButtonAction.buttonText)
            alertRightButton?.setOnClickListener { viewPresenter.handleButtonClick(rightButtonAction) }
        }
    }

    override fun onBackPressed() {
        viewPresenter.handleBackPressed()
    }

    override fun startRefusalActivity() {
        startActivityForResult(
            Intent(this, RefusalActivity::class.java),
            RequestCode.REFUSAL.value
        )
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

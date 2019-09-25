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
import com.simprints.id.activities.coreexitform.CoreExitFormActivity
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.core.response.CoreResponse
import com.simprints.id.exitformhandler.ExitFormHandler
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import kotlinx.android.synthetic.main.activity_alert.*
import javax.inject.Inject

class AlertActivity : AppCompatActivity(), AlertContract.View {

    override lateinit var viewPresenter: AlertContract.Presenter
    private lateinit var alertTypeType: AlertType
    @Inject lateinit var exitFormHandler: ExitFormHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val app = application as Application

        injectDependencies(app.component)

        alertTypeType = intent.extras?.let {
            it.get(AlertActRequest.BUNDLE_KEY) as AlertActRequest
        }?.alertType ?: AlertType.UNEXPECTED_ERROR

        viewPresenter = AlertPresenter(this, app.component, alertTypeType)
        viewPresenter.start()
    }

    private fun injectDependencies(component: AppComponent) {
        component.inject(this)
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
    }

    override fun startCoreExitFormActivity() {
        startActivityForResult(Intent(this, CoreExitFormActivity::class.java), CoreRequestCode.EXIT_FORM.value)
    }

    override fun startFingerprintExitFormActivity() {
        startActivityForResult(Intent(this, FingerprintExitFormActivity::class.java), CoreRequestCode.EXIT_FORM.value)
    }

    override fun startFaceExitFormActivity() {
        startActivityForResult(Intent(this, FaceExitFormActivity::class.java), CoreRequestCode.EXIT_FORM.value)
    }


    override fun closeActivityAfterCloseButton() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResponse.BUNDLE_KEY, AlertActResponse(alertTypeType, AlertActResponse.ButtonAction.CLOSE))
        })

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        exitFormHandler.buildExitFormResponseForCore(data)?.let {
            setResultAndFinish(it)
        }
    }

    override fun finishWithTryAgain() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResponse.BUNDLE_KEY, AlertActResponse(alertTypeType, AlertActResponse.ButtonAction.TRY_AGAIN))
        })

        finish()
    }

    override fun openWifiSettings() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_WIFI_SETTINGS
        }
        startActivity(intent)
    }

    private fun setResultAndFinish(coreResponse: CoreResponse) {
        setResult(Activity.RESULT_OK, buildIntentForResponse(coreResponse))
        finish()
    }

    private fun buildIntentForResponse(coreResponse: CoreResponse) = Intent().apply {
        putExtra(CoreResponse.CORE_STEP_BUNDLE, coreResponse)
    }
}

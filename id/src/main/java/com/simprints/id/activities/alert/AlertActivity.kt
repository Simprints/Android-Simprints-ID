package com.simprints.id.activities.alert

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.request.AlertActRequest
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.databinding.ActivityAlertBinding
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import javax.inject.Inject

class AlertActivity : BaseSplitActivity(), AlertContract.View {

    private val binding by viewBinding(ActivityAlertBinding::inflate)

    override lateinit var viewPresenter: AlertContract.Presenter
    private lateinit var alertTypeType: AlertType
    @Inject lateinit var exitFormHelper: ExitFormHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        (application as Application).component.inject(this)
        title = getString(R.string.alert_title)

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
    override fun setLayoutBackgroundColor(@ColorInt color: Int) = binding.alertLayout.setBackgroundColor(color)
    override fun setLeftButtonBackgroundColor(@ColorInt color: Int) = binding.alertLeftButton.setBackgroundColor(color)
    override fun setRightButtonBackgroundColor(@ColorInt color: Int) = binding.alertRightButton.setBackgroundColor(color)
    override fun setAlertTitleWithStringRes(@StringRes stringRes: Int) { binding.alertTitle.text = getString(stringRes) }
    override fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int) = binding.alertImage.setImageResource(drawableId)
    override fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?) {
        if (alertHintDrawableId != null) {
            binding.hintGraphic.setImageResource(alertHintDrawableId)
        } else {
            binding.hintGraphic.visibility = View.GONE
        }
    }

    override fun setAlertMessageWithStringRes(@StringRes stringRes: Int,  params: Array<Any>) {
        binding.message.text = String.format(getString(stringRes), *params)
    }

    override fun getTranslatedString(@StringRes stringRes: Int) = getString(stringRes)

    override fun initLeftButton(leftButtonAction: AlertActivityViewModel.ButtonAction) =
        initButtonWithButtonAction(binding.alertLeftButton, leftButtonAction)

    override fun initRightButton(rightButtonAction: AlertActivityViewModel.ButtonAction) =
        initButtonWithButtonAction(binding.alertRightButton, rightButtonAction)

    private fun initButtonWithButtonAction(button: TextView, buttonAction: AlertActivityViewModel.ButtonAction) {
        if (buttonAction !is AlertActivityViewModel.ButtonAction.None) {
            button.text = getString(buttonAction.buttonText)
            button.setOnClickListener { viewPresenter.handleButtonClick(buttonAction) }
        } else {
            button.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        viewPresenter.handleBackButton()
    }

    override fun startExitForm(exitFormActivityClass: String?) {
        exitFormActivityClass?.let {
            startActivityForResult(
                Intent().setClassName(this, exitFormActivityClass),
                CoreRequestCode.EXIT_FORM.value
            )
        }
    }

    override fun closeActivityAfterCloseButton() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AlertActResponse.BUNDLE_KEY, AlertActResponse(alertTypeType, AlertActResponse.ButtonAction.CLOSE))
        })

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            WIFI_SETTINGS_REQUEST_CODE -> finishWithTryAgain()
            CoreRequestCode.EXIT_FORM.value -> {
                exitFormHelper.buildExitFormResponseForCore(data)?.let {
                    setResultAndFinish(it)
                }
            }
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

    override fun openWifiSettingsAndFinishWithTryAgain() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_WIFI_SETTINGS
        }
        startActivityForResult(intent, WIFI_SETTINGS_REQUEST_CODE)
    }

    private fun setResultAndFinish(coreResponse: CoreResponse) {
        setResult(Activity.RESULT_OK, buildIntentForResponse(coreResponse))
        finish()
    }

    private fun buildIntentForResponse(coreResponse: CoreResponse) = Intent().apply {
        putExtra(CoreResponse.CORE_STEP_BUNDLE, coreResponse)
    }

    companion object {
        private const val WIFI_SETTINGS_REQUEST_CODE = 100
    }
}

package com.simprints.id.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper.extractPotentialAlertScreenResponse
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.login.response.LoginActivityResponse
import com.simprints.id.activities.login.response.LoginActivityResponse.Companion.RESULT_CODE_LOGIN_SUCCEED
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.viewmodel.LoginViewModel
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.activities.qrcapture.QrCaptureActivity
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.databinding.ActivityLoginBinding
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.secure.AuthenticationHelperImpl.Companion.PREFS_ESTIMATED_OUTAGE
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.showToast
import com.simprints.id.tools.utils.getFormattedEstimatedOutage
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.url.BaseUrlProvider
import javax.inject.Inject

class LoginActivity : BaseSplitActivity() {

    private val binding by viewBinding(ActivityLoginBinding::inflate)

    @Inject
    lateinit var viewModelFactory: LoginViewModelFactory

    @Inject
    lateinit var loginActivityHelper: LoginActivityHelper

    @Inject
    lateinit var baseUrlProvider: BaseUrlProvider

    @Inject
    lateinit var idPreferencesManager: IdPreferencesManager

    private val loginActRequest: LoginActivityRequest by lazy {
        intent.extras?.getParcelable<LoginActivityRequest>(LoginActivityRequest.BUNDLE_KEY)
            ?: throw InvalidAppRequest()
    }

    private lateinit var progressDialog: SimProgressDialog
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        setContentView(binding.root)

        baseUrlProvider.resetApiBaseUrl()
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        initUI()
        observeSignInResult()
    }

    private fun initUI() {
        setUpTexts()
        setUpButtons()
        progressDialog = SimProgressDialog(this)
        binding.loginEditTextUserId.setText(loginActRequest.userIdFromIntent)
    }

    private fun setUpTexts() {
        binding.apply {
            loginEditTextUserId.hint = getString(R.string.login_user_id_hint)
            loginEditTextProjectSecret.hint = getString(R.string.login_secret_hint)
            loginButtonScanQr.text = getString(R.string.scan_qr)
            loginButtonSignIn.text = getString(R.string.login)
            loginEditTextProjectId.hint = getString(R.string.login_id_hint)
            loginImageViewLogo.contentDescription = getString(R.string.simprints_logo)
        }
    }

    private fun setUpButtons() {
        binding.loginButtonScanQr.setOnClickListener {
            logMessageForCrashReport("Scan QR button clicked")
            scanQrCode()
        }

        binding.loginButtonSignIn.setOnClickListener {
            logMessageForCrashReport("Login button clicked")
            signIn()
        }
    }

    private fun observeSignInResult() {
        viewModel.getSignInResult().observe(this) {
            handleSignInResult(it)
        }
    }

    private fun handleSignInResult(result: Result) {
        when (result) {
            Result.AUTHENTICATED -> handleSignInSuccess()
            Result.OFFLINE -> handleSignInFailedNoConnection()
            Result.BAD_CREDENTIALS -> handleSignInFailedInvalidCredentials()
            Result.TECHNICAL_FAILURE -> handleSignInFailedServerError()
            Result.SAFETYNET_INVALID_CLAIM,
            Result.SAFETYNET_UNAVAILABLE -> handleSafetyNetDownError()
            Result.BACKEND_MAINTENANCE_ERROR -> handleSignInFailedBackendMaintenanceError(
                idPreferencesManager.getSharedPreference(PREFS_ESTIMATED_OUTAGE,0L)
            )
            Result.UNKNOWN -> handleSignInFailedUnknownReason()
        }
    }

    private fun scanQrCode() {
        val intent = QrCaptureActivity.getIntent(this)
        startActivityForResult(intent, QR_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val potentialAlertScreenResponse = extractPotentialAlertScreenResponse(data)

        if (potentialAlertScreenResponse != null) {
            setResult(resultCode, data)
            finish()
        } else if (requestCode == QR_REQUEST_CODE) {
            data?.let {
                handleQrScanResult(resultCode, it)
            } ?: showErrorForQRCodeFailed()
        }
    }

    private fun handleQrScanResult(resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK)
            processQrScanResponse(data)
        else
            showErrorForQRCodeFailed()
    }

    private fun processQrScanResponse(response: Intent) {
        try {
            val qrCodeResponse = loginActivityHelper.tryParseQrCodeResponse(response)
            Simber.d("QR code response: $qrCodeResponse")
            val projectId = qrCodeResponse.projectId
            val projectSecret = qrCodeResponse.projectSecret
            baseUrlProvider.setApiBaseUrl(qrCodeResponse.apiBaseUrl)

            updateProjectInfoOnTextFields(projectId, projectSecret)
            logMessageForCrashReport("QR scanning successful")
        } catch (t: Throwable) {
            showErrorForInvalidQRCode()
            logMessageForCrashReport("QR scanning unsuccessful")
        }
    }

    private fun updateProjectInfoOnTextFields(projectId: String, projectSecret: String) {
        binding.loginEditTextProjectId.setText(projectId)
        binding.loginEditTextProjectSecret.setText(projectSecret)
    }

    private fun showErrorForInvalidQRCode() {
        showToast(R.string.login_invalid_qr_code)
    }

    private fun showErrorForQRCodeFailed() {
        showToast(R.string.login_qr_code_scanning_problem)
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(CrashReportTag.LOGIN.name).i(message)
    }

    private fun signIn() {
        progressDialog.show()
        val projectId = binding.loginEditTextProjectId.text.toString()
        val userId = binding.loginEditTextUserId.text.toString()
        val projectSecret = binding.loginEditTextProjectSecret.text.toString()
        val projectIdFromIntent = loginActRequest.projectIdFromIntent

        val areMandatoryCredentialsPresent = loginActivityHelper.areMandatoryCredentialsPresent(
            projectId, projectSecret, userId
        )

        val areSuppliedProjectIdAndProjectIdFromIntentEqual =
            loginActivityHelper.areSuppliedProjectIdAndProjectIdFromIntentEqual(
                projectId, projectIdFromIntent
            )

        if (!areMandatoryCredentialsPresent)
            handleMissingCredentials()
        else if (!areSuppliedProjectIdAndProjectIdFromIntentEqual)
            handleProjectIdMismatch()
        else
            viewModel.signIn(userId, projectId, projectSecret, deviceId)
    }

    private fun handleMissingCredentials() {
        progressDialog.dismiss()
        showToast(R.string.login_missing_credentials)
    }

    private fun handleProjectIdMismatch() {
        progressDialog.dismiss()
        showToast(R.string.login_project_id_intent_mismatch)
    }

    private fun handleSignInSuccess() {
        progressDialog.dismiss()
        setResult(RESULT_CODE_LOGIN_SUCCEED)
        finish()
    }

    private fun handleSignInFailedNoConnection() {
        progressDialog.dismiss()
        showToast(R.string.login_no_network)
    }

    private fun handleSignInFailedInvalidCredentials() {
        progressDialog.dismiss()
        showToast(R.string.login_invalid_credentials)
    }

    private fun handleSignInFailedServerError() {
        progressDialog.dismiss()
        binding.errorCard.isVisible = false
        showToast(R.string.login_server_error)
    }

    private fun handleSignInFailedBackendMaintenanceError(estimatedOutage: Long) {
        progressDialog.dismiss()
        binding.apply {
            errorCard.isVisible = true
            errorTextView.text = if (estimatedOutage != 0L) getString(
                R.string.error_backend_maintenance_with_time_message, getFormattedEstimatedOutage(
                    estimatedOutage
                )
            ) else getString(R.string.error_backend_maintenance_message)
        }
    }

    private fun handleSafetyNetDownError() {
        progressDialog.dismiss()
        binding.errorCard.isVisible = false
        launchAlert(this, AlertType.SAFETYNET_ERROR)
    }

    private fun handleSignInFailedUnknownReason() {
        progressDialog.dismiss()
        binding.errorCard.isVisible = false
        launchAlert(this, AlertType.UNEXPECTED_ERROR)
    }

    override fun onBackPressed() {
        val response = AppErrorResponse(AppErrorResponse.Reason.LOGIN_NOT_COMPLETE)
        setErrorResponseInActivityResultAndFinish(response)
    }

    private fun setErrorResponseInActivityResultAndFinish(appErrorResponse: AppErrorResponse) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(LoginActivityResponse.BUNDLE_KEY, appErrorResponse)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog.dismiss()
    }

    private companion object {
        const val QR_REQUEST_CODE = 0
    }

}

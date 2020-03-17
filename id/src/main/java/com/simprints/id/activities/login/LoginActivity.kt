package com.simprints.id.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import javax.inject.Inject

class LoginActivity : AppCompatActivity(R.layout.activity_login) {

    @Inject lateinit var viewModelFactory: LoginViewModelFactory
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var loginActivityHelper: LoginActivityHelper

    private val loginActRequest: LoginActivityRequest by lazy {
        intent.extras?.getParcelable<LoginActivityRequest>(LoginActivityRequest.BUNDLE_KEY)
            ?: throw InvalidAppRequest()
    }

    private lateinit var progressDialog: SimProgressDialog
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)
        initUI()
        observeSignInResult()
    }

    private fun initUI() {
        setUpTexts()
        setUpButtons()
        progressDialog = SimProgressDialog(this)
        loginEditTextUserId.setText(loginActRequest.userIdFromIntent)
    }

    private fun setUpTexts() {
        with(androidResourcesHelper) {
            loginEditTextUserId.hint = getString(R.string.login_user_id_hint)
            loginEditTextProjectSecret.hint = getString(R.string.login_secret_hint)
            loginButtonScanQr.text = getString(R.string.scan_qr)
            loginButtonSignIn.text = getString(R.string.login)
            loginEditTextProjectId.hint = getString(R.string.login_id_hint)
            loginImageViewLogo.contentDescription = getString(R.string.simprints_logo)
        }
    }

    private fun setUpButtons() {
        loginButtonScanQr.setOnClickListener {
            logMessageForCrashReport("Scan QR button clicked")
            openScannerApp()
        }

        loginButtonSignIn.setOnClickListener {
            logMessageForCrashReport("Login button clicked")
            signIn()
        }
    }

    private fun observeSignInResult() {
        viewModel.getSignInResult().observe(this, Observer {
            handleSignInResult(it)
        })
    }

    private fun handleSignInResult(result: AuthenticationEvent.Result) {
        when (result) {
            AuthenticationEvent.Result.AUTHENTICATED -> handleSignInSuccess()
            AuthenticationEvent.Result.OFFLINE -> handleSignInFailedNoConnection()
            AuthenticationEvent.Result.BAD_CREDENTIALS -> handleSignInFailedInvalidCredentials()
            AuthenticationEvent.Result.TECHNICAL_FAILURE -> handleSignInFailedServerError()
            AuthenticationEvent.Result.SAFETYNET_INVALID_CLAIM,
            AuthenticationEvent.Result.SAFETYNET_UNAVAILABLE -> handleSafetyNetDownError()
            AuthenticationEvent.Result.UNKNOWN -> handleSignInFailedUnknownReason()
        }
    }

    private fun openScannerApp() {
        loginActivityHelper.tryGetScannerAppIntent(packageManager)?.let { scannerAppIntent ->
            startActivityForResult(scannerAppIntent, QR_REQUEST_CODE)
        } ?: openScannerAppOnPlayStore(loginActivityHelper.getIntentForScannerAppOnPlayStore())
    }

    private fun openScannerAppOnPlayStore(intent: Intent) {
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val potentialAlertScreenResponse = extractPotentialAlertScreenResponse(data)

        if (potentialAlertScreenResponse != null) {
            setResult(resultCode, data)
            finish()
        } else if (requestCode == QR_REQUEST_CODE) {
            data?.let {
                handleScannerAppResult(resultCode, it)
            }
        }
    }

    private fun handleScannerAppResult(resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK)
            processScannerAppResponse(data)
        else
            showErrorForQRCodeFailed()
    }

    private fun processScannerAppResponse(response: Intent) {
        try {
            val credentialsResponse = loginActivityHelper.tryParseQrCodeResponse(response)
            val projectId = credentialsResponse.projectId
            val projectSecret = credentialsResponse.projectSecret

            updateProjectInfoOnTextFields(projectId, projectSecret)
            logMessageForCrashReport("QR scanning successful")
        } catch (e: JSONException) {
            showErrorForInvalidQRCode()
            logMessageForCrashReport("QR scanning unsuccessful")
        }
    }

    private fun updateProjectInfoOnTextFields(projectId: String, projectSecret: String) {
        loginEditTextProjectId.setText(projectId)
        loginEditTextProjectSecret.setText(projectSecret)
    }

    private fun showErrorForInvalidQRCode() {
        showToast(androidResourcesHelper, R.string.login_invalid_qr_code)
    }

    private fun showErrorForQRCodeFailed() {
        showToast(androidResourcesHelper, R.string.login_qr_code_scanning_problem)
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.LOGIN,
            CrashReportTrigger.UI,
            message = message
        )
    }

    private fun signIn() {
        progressDialog.show()
        val projectId = loginEditTextProjectId.text.toString()
        val userId = loginEditTextUserId.text.toString()
        val projectSecret = loginEditTextProjectSecret.text.toString()
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
            viewModel.signIn(projectId, userId, projectSecret)
    }

    private fun handleMissingCredentials() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_missing_credentials)
    }

    private fun handleProjectIdMismatch() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_project_id_intent_mismatch)
    }

    private fun handleSignInSuccess() {
        progressDialog.dismiss()
        setResult(RESULT_CODE_LOGIN_SUCCEED)
        finish()
    }

    private fun handleSignInFailedNoConnection() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_no_network)
    }

    private fun handleSignInFailedInvalidCredentials() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_invalid_credentials)
    }

    private fun handleSignInFailedServerError() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_server_error)
    }

    private fun handleSafetyNetDownError() {
        progressDialog.dismiss()
        launchAlert(this, AlertType.SAFETYNET_ERROR)
    }

    private fun handleSignInFailedUnknownReason() {
        progressDialog.dismiss()
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

    private companion object {
        const val QR_REQUEST_CODE = 0
    }

}

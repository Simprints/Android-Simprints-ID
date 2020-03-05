package com.simprints.id.activities.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper.extractPotentialAlertScreenResponse
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.login.response.LoginActivityResponse
import com.simprints.id.activities.login.response.LoginActivityResponse.Companion.RESULT_CODE_LOGIN_SUCCEED
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
import com.simprints.id.tools.extensions.scannerAppIntent
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class LoginActivity : AppCompatActivity(R.layout.activity_login) {

    @Inject lateinit var viewModelFactory: LoginViewModelFactory
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var crashReportManager: CrashReportManager

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
    }

    private fun initUI() {
        setTextInLayout()
        progressDialog = SimProgressDialog(this)
        loginEditTextUserId.setText(loginActRequest.userIdFromIntent)
        loginButtonScanQr.setOnClickListener {
            logMessageForCrashReport("Scan QR button clicked")
            openScanQRApp()
        }
        loginButtonSignIn.setOnClickListener {
            logMessageForCrashReport("Login button clicked")
            signIn()
        }
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
            loginEditTextUserId.hint = getString(R.string.login_user_id_hint)
            loginEditTextProjectSecret.hint = getString(R.string.login_secret_hint)
            loginButtonScanQr.text = getString(R.string.scan_qr)
            loginButtonSignIn.text = getString(R.string.login)
            loginEditTextProjectId.hint = getString(R.string.login_id_hint)
            loginImageViewLogo.contentDescription = getString(R.string.simprints_logo)
        }
    }

    private fun openScanQRApp() {
        val intent = packageManager.scannerAppIntent()
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, QR_REQUEST_CODE)
        } else {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(GOOGLE_PLAY_LINK_FOR_QR_APP)
                )
            )
        }
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
        val scannedText = data.getStringExtra(QR_RESULT_KEY)

        if (resultCode == Activity.RESULT_OK)
            processQRScannerAppResponse(scannedText)
        else
            showErrorForQRCodeFailed()
    }

    /**
     * Valid Scanned Text Format:
     * {"projectId":"someProjectId","projectSecret":"someSecret"}
     **/
    private fun processQRScannerAppResponse(scannedText: String) {
        try {
            with(JSONObject(scannedText)) {
                val potentialProjectId = getString(PROJECT_ID_JSON_KEY)
                val potentialProjectSecret = getString(PROJECT_SECRET_JSON_KEY)
                updateProjectInfoOnTextFields(potentialProjectId, potentialProjectSecret)
            }

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

        if (!areMandatoryCredentialsPresent(projectId, projectSecret, userId)) {
            handleMissingCredentials()
        } else if (projectId != loginActRequest.projectIdFromIntent) {
            handleSignInFailedProjectIdIntentMismatch()
        } else {
            lifecycleScope.launch {
                val result = viewModel.signIn(projectId, userId, projectSecret)
                handleSignInResult(result)
            }
        }
    }

    private fun areMandatoryCredentialsPresent(
        possibleProjectId: String,
        possibleProjectSecret: String,
        possibleUserId: String
    ): Boolean {
        return possibleProjectId.isNotEmpty()
            && possibleProjectSecret.isNotEmpty()
            && possibleUserId.isNotEmpty()
    }

    private fun handleMissingCredentials() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_missing_credentials)
    }

    private fun handleSignInFailedProjectIdIntentMismatch() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_project_id_intent_mismatch)
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
        const val QR_RESULT_KEY = "SCAN_RESULT"
        const val GOOGLE_PLAY_LINK_FOR_QR_APP =
            "https://play.google.com/store/apps/details?id=com.google.zxing.client.android"
        const val PROJECT_ID_JSON_KEY = "projectId"
        const val PROJECT_SECRET_JSON_KEY = "projectSecret"
    }

}

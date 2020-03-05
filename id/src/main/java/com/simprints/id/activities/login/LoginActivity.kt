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
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.scannerAppIntent
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginActivity : AppCompatActivity(R.layout.activity_login), LoginContract.View {

    companion object {
        const val QR_REQUEST_CODE: Int = 0
        const val QR_RESULT_KEY = "SCAN_RESULT"
        const val GOOGLE_PLAY_LINK_FOR_QR_APP =
            "https://play.google.com/store/apps/details?id=com.google.zxing.client.android"
    }

    override lateinit var viewPresenter: LoginContract.Presenter

    @Inject lateinit var preferences: PreferencesManager
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var viewModelFactory: LoginViewModelFactory

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

    private fun initUI() {
        setTextInLayout()
        progressDialog = SimProgressDialog(this)
        loginEditTextUserId.setText(loginActRequest.userIdFromIntent)
        loginButtonScanQr.setOnClickListener {
            //viewPresenter.logMessageForCrashReportWithUITrigger("Scan QR button clicked")
            openScanQRApp()
        }
        loginButtonSignIn.setOnClickListener {
            //viewPresenter.logMessageForCrashReportWithUITrigger("Login button clicked")
            handleSignInStart()
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

    private fun handleSignInStart() {
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

    fun handleScannerAppResult(resultCode: Int, data: Intent) =
        runOnUiThread {
            val scannedText = data.getStringExtra(QR_RESULT_KEY)

            if (resultCode == Activity.RESULT_OK) {
                //viewPresenter.processQRScannerAppResponse(scannedText)
            } else {
                showErrorForQRCodeFailed()
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

    private fun showErrorForQRCodeFailed() {
        showToast(androidResourcesHelper, R.string.login_qr_code_scanning_problem)
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

    override fun showErrorForInvalidQRCode() {
        showToast(androidResourcesHelper, R.string.login_invalid_qr_code)
    }

    override fun updateProjectSecretInTextView(projectSecret: String) {
        loginEditTextProjectSecret.setText(projectSecret)
    }

    override fun updateProjectIdInTextView(projectId: String) {
        loginEditTextProjectId.setText(projectId)
    }

    override fun handleMissingCredentials() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_missing_credentials)
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

    private fun handleSignInFailedServerError() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_server_error)
    }

    private fun handleSignInFailedInvalidCredentials() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_invalid_credentials)
    }

    private fun handleSignInFailedProjectIdIntentMismatch() {
        progressDialog.dismiss()
        showToast(androidResourcesHelper, R.string.login_project_id_intent_mismatch)
    }

    private fun handleSignInFailedUnknownReason() {
        progressDialog.dismiss()
        launchAlert(this, AlertType.UNEXPECTED_ERROR)
    }

    private fun handleSafetyNetDownError() {
        progressDialog.dismiss()
        launchAlert(this, AlertType.SAFETYNET_ERROR)
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

}

package com.simprints.id.activities.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.safetynet.SafetyNet
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper.extractPotentialAlertScreenResponse
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.login.response.LoginActivityResponse
import com.simprints.id.activities.login.response.LoginActivityResponse.Companion.RESULT_CODE_LOGIN_SUCCEED
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.scannerAppIntent
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), LoginContract.View {

    companion object {
        const val QR_REQUEST_CODE: Int = 0
        const val QR_RESULT_KEY = "SCAN_RESULT"
        const val GOOGLE_PLAY_LINK_FOR_QR_APP =
            "https://play.google.com/store/apps/details?id=com.google.zxing.client.android"
    }

    override lateinit var viewPresenter: LoginContract.Presenter
    @Inject lateinit var preferences: PreferencesManager
    @Inject lateinit var secureApiInterface: SecureApiInterface
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    val app by lazy {
        application as Application
    }

    private lateinit var progressDialog: SimProgressDialog
    private lateinit var loginActRequest: LoginActivityRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTextInLayout()

        loginActRequest = this.intent.extras?.getParcelable(LoginActivityRequest.BUNDLE_KEY)
            ?: throw InvalidAppRequest()

        val component = (application as Application).component
        component.inject(this)

        initUI()

        val projectAuthenticator = ProjectAuthenticator(
            component,
            SafetyNet.getClient(this),
            secureApiInterface)

        viewPresenter = LoginPresenter(this, component, projectAuthenticator)
        viewPresenter.start()
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
        progressDialog = SimProgressDialog(this)
        loginEditTextUserId.setText(loginActRequest.userIdFromIntent)
        loginButtonScanQr.setOnClickListener {
            viewPresenter.logMessageForCrashReportWithUITrigger("Scan QR button clicked")
            viewPresenter.openScanQRApp()
        }
        loginButtonSignIn.setOnClickListener {
            viewPresenter.logMessageForCrashReportWithUITrigger("Login button clicked")
            handleSignInStart()
        }
    }

    override fun handleOpenScanQRApp() {
        val intent = packageManager.scannerAppIntent()
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, QR_REQUEST_CODE)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse(GOOGLE_PLAY_LINK_FOR_QR_APP)))
        }
    }

    private fun handleSignInStart() {
        progressDialog.show()
        val userId = loginEditTextUserId.text.toString()
        val projectId = loginEditTextProjectId.text.toString()
        val projectSecret = loginEditTextProjectSecret.text.toString()
        viewPresenter.signIn(userId, projectId, projectSecret, loginActRequest.projectIdFromIntent)
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
                viewPresenter.processQRScannerAppResponse(scannedText)
            } else {
                showErrorForQRCodeFailed()
            }
        }

    private fun showErrorForQRCodeFailed() {
        showToast(R.string.login_qr_code_scanning_problem)
    }

    override fun showErrorForInvalidQRCode() {
        showToast(R.string.login_invalid_qr_code)
    }

    override fun updateProjectSecretInTextView(projectSecret: String) {
        loginEditTextProjectSecret.setText(projectSecret)
    }

    override fun updateProjectIdInTextView(projectId: String) {
        loginEditTextProjectId.setText(projectId)
    }

    override fun handleMissingCredentials() {
        progressDialog.dismiss()
        showToast(R.string.login_missing_credentials)
    }

    override fun handleSignInSuccess() {
        progressDialog.dismiss()
        setResult(RESULT_CODE_LOGIN_SUCCEED)
        finish()
    }

    override fun handleSignInFailedNoConnection() {
        progressDialog.dismiss()
        showToast(R.string.login_no_network)
    }

    override fun handleSignInFailedServerError() {
        progressDialog.dismiss()
        showToast(R.string.login_server_error)
    }

    override fun handleSignInFailedInvalidCredentials() {
        progressDialog.dismiss()
        showToast(R.string.login_invalid_credentials)
    }

    override fun handleSignInFailedProjectIdIntentMismatch() {
        progressDialog.dismiss()
        showToast(R.string.login_project_id_intent_mismatch)
    }

    override fun handleSignInFailedUnknownReason() {
        progressDialog.dismiss()
        launchAlert(this, AlertType.UNEXPECTED_ERROR)
    }

    override fun handleSafetyNetDownError() {
        progressDialog.dismiss()
        launchAlert(this, AlertType.SAFETYNET_ERROR)
    }

    override fun onBackPressed() {
        viewPresenter.handleBackPressed()
    }

    override fun setErrorResponseInActivityResultAndFinish(appErrorResponse: AppErrorResponse) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(LoginActivityResponse.BUNDLE_KEY, appErrorResponse)
        })
        finish()
    }
}

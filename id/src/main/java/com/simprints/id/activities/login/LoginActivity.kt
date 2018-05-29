package com.simprints.id.activities.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.safetynet.SafetyNet
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.extensions.scannerAppIntent
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), LoginContract.View {

    companion object {
        const val LOGIN_SUCCEED: Int = 1
        const val QR_REQUEST_CODE: Int = 0
        const val QR_RESULT_KEY = "SCAN_RESULT"
        const val GOOGLE_PLAY_LINK_FOR_QR_APP =
            "https://play.google.com/store/apps/details?id=com.google.zxing.client.android"
    }

    override lateinit var viewPresenter: LoginContract.Presenter

    private var possibleLegacyProjectId: String? = null
    val app by lazy {
        application as Application
    }

    private lateinit var progressDialog: SimProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initUI()

        intent.getStringExtra(IntentKeys.loginActivityLegacyProjectIdKey)?.let {
            if (it.isNotEmpty()) {
                possibleLegacyProjectId = it
            }
        }

        val projectAuthenticator = LegacyCompatibleProjectAuthenticator(
            app.loginInfoManager,
            app.dbManager,
            app.secureDataManager,
            SafetyNet.getClient(this))

        viewPresenter = LoginPresenter(this, app.loginInfoManager, app.analyticsManager, projectAuthenticator)
         viewPresenter.start()
    }

    private fun initUI() {
        progressDialog = SimProgressDialog(this)
        loginEditTextUserId.setText(app.dataManager.preferences.userId)
        loginButtonScanQr.setOnClickListener { viewPresenter.openScanQRApp() }
        loginButtonSignIn.setOnClickListener { handleSignInStart() }
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
        viewPresenter.signIn(userId, projectId, projectSecret, app.dataManager.preferences.projectId, possibleLegacyProjectId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == QR_REQUEST_CODE) {
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
        setResult(LOGIN_SUCCEED)
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
        launchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
    }
}

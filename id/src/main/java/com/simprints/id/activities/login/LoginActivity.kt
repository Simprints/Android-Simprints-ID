package com.simprints.id.activities.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.safetynet.SafetyNet
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.exceptions.safe.activities.InvalidScannedQRCodeText
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.extensions.scannerAppIntent
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

        val projectAuthenticator = LegacyCompatibleProjectAuthenticator(app.secureDataManager, app.dataManager, SafetyNet.getClient(this))
        viewPresenter = LoginPresenter(this, app.secureDataManager, app.analyticsManager, projectAuthenticator)
        viewPresenter.start()
    }

    private fun initUI() {
        progressDialog = SimProgressDialog(this)
        loginEditTextUserId.setText(app.dataManager.userId)
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
        viewPresenter.signIn(userId, projectId, projectSecret, possibleLegacyProjectId)
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
            try {
                val scannedText = data.getStringExtra(QR_RESULT_KEY)

                if (resultCode == Activity.RESULT_OK) {
                    viewPresenter.processQRScannerAppResponse(scannedText)
                } else {
                    showErrorForQRCodeFailed()
                }
            } catch (e: InvalidScannedQRCodeText) {
                showErrorForInvalidQRCode()
            }
        }

    private fun showErrorForQRCodeFailed() {
        showToast(R.string.login_qr_code_scanning_problem)
    }

    private fun showErrorForInvalidQRCode() {
        showToast(R.string.login_invalid_qr_code)
    }

    override fun updateProjectSecretInTextView(projectSecret: String) {
        loginEditTextProjectSecret.setText(projectSecret)
    }

    override fun updateProjectIdInTextView(projectId: String) {
        loginEditTextProjectId.setText(projectId)
    }

    override fun handleMissingCredentials() {
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
        launchAlert(ALERT_TYPE.INVALID_PROJECT_ID)
    }

    override fun handleSignInFailedUnknownReason() {
        progressDialog.dismiss()
        launchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    private fun showToast(stringRes: Int) =
        runOnUiThread {
            Toast.makeText(this, stringRes, Toast.LENGTH_LONG).show()
        }
}

package com.simprints.id.activities.login

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.safetynet.SafetyNet
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.Token
import com.simprints.id.tools.extensions.scannerAppIntent
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.indeterminateProgressDialog

class LoginActivity : AppCompatActivity(), LoginContract.View {

    companion object {
        public const val LOGIN_SUCCESSED: Int = 1
        public const val LOGIN_REQUEST_CODE: Int = 1
        private const val QR_REQUEST_CODE: Int = 0
    }

    lateinit var viewPresenter: LoginContract.Presenter

    val app by lazy {
        application as Application
    }
    val userId by lazy {
        app.dataManager.userId
    }

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val app = application as Application
        viewPresenter = LoginPresenter(this, app.secureDataManager, ProjectAuthenticator(app.secureDataManager, app.dataManager), SafetyNet.getClient(this))
        viewPresenter.start()

        initUI()
    }

    override fun setPresenter(presenter: LoginContract.Presenter) {
        viewPresenter = presenter
    }

    private fun initUI() {
        loginEditTextUserId.setText(userId)
        loginButtonScanQr.setOnClickListener { viewPresenter.userDidWantToOpenScanQRApp() }
        loginButtonSignIn.setOnClickListener {
            val projectId = loginEditTextProjectId.text.toString()
            val projectSecret = loginEditTextProjectSecret.text.toString()
            viewPresenter.userDidWantToSignIn(projectId, projectSecret, userId, app.dataManager.apiKey)
        }
    }

    override fun openScanQRApp() {
        val intent = packageManager.scannerAppIntent()
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, QR_REQUEST_CODE)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.zxing.client.android")))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == QR_REQUEST_CODE) {
            if (data == null) return
            handleScannerAppResult(resultCode, data)
        }
    }

    private fun handleScannerAppResult(resultCode: Int, data: Intent) {

        runOnUiThread {
            try {
                val scannedText = data.getStringExtra("SCAN_RESULT")
                val isResultValid = resultCode == Activity.RESULT_OK && scannedText != null

                if (isResultValid) {
                    viewPresenter.processQRScannerAppResponse(scannedText)
                } else {
                    throw Exception("Invalid result from the QRCode app")
                }
            } catch (e: Exception) {
                showErrorForInvalidQRCode()
            }
        }
    }

    private fun showErrorForInvalidQRCode() {
        Toast.makeText(this, R.string.login_invalidQrCode, Toast.LENGTH_SHORT).show()
    }

    override fun updateProjectSecretInTextView(projectSecret: String) {
        loginEditTextProjectSecret.setText(projectSecret)
    }

    override fun updateProjectIdInTextView(projectId: String) {
        loginEditTextProjectId.setText(projectId)
    }

    override fun showToast(stringRes: Int) {
        Toast.makeText(this, stringRes, Toast.LENGTH_SHORT).show()
    }

    override fun showProgressDialog(title: Int, message: Int) {
        progressDialog = indeterminateProgressDialog(title, message)
        progressDialog.show()
    }

    override fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    override fun returnSuccessfulResult(token: Token) {
        val resultData = Intent()
        resultData.putExtra(IntentKeys.loginActivityTokenReturn, token.value)

        setResult(LOGIN_SUCCESSED, resultData)
        finish()
    }
}

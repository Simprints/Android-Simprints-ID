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
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.scannerAppIntent
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), LoginContract.View {

    companion object {
        const val LOGIN_SUCCEED: Int = 1
        const val QR_REQUEST_CODE: Int = 0
        const val LEGACY_API_KEY_PARAM = "legacyApiKey"
        const val QR_RESULT_KEY = "SCAN_RESULT"
        const val GOOGLE_PLAY_LINK_FOR_QR_APP =
            "https://play.google.com/store/apps/details?id=com.google.zxing.client.android"
    }

    override lateinit var viewPresenter: LoginContract.Presenter

    private var possibleLegacyApiKey: String? = null
    val app by lazy {
        application as Application
    }

    private lateinit var progressDialog: SimProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initUI()

        intent.getStringExtra(LEGACY_API_KEY_PARAM)?.let {
            if (it.isNotEmpty()) {
                possibleLegacyApiKey = it
            }
        }

        val projectAuthenticator = ProjectAuthenticator(app.secureDataManager, app.dataManager, SafetyNet.getClient(this))
        viewPresenter = LoginPresenter(this, app.secureDataManager, projectAuthenticator)
        viewPresenter.start()
    }

    private fun initUI() {
        progressDialog = SimProgressDialog(this)
        loginEditTextUserId.setText(app.dataManager.userId)
        loginButtonScanQr.setOnClickListener { viewPresenter.userDidWantToOpenScanQRApp() }
        loginButtonSignIn.setOnClickListener {
            val projectId = loginEditTextProjectId.text.toString()
            val projectSecret = loginEditTextProjectSecret.text.toString()
            val userId = loginEditTextUserId.text.toString()
            viewPresenter.userDidWantToSignIn(projectId, projectSecret, userId, possibleLegacyApiKey)
        }
    }

    override fun openScanQRApp() {
        val intent = packageManager.scannerAppIntent()
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, QR_REQUEST_CODE)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse(GOOGLE_PLAY_LINK_FOR_QR_APP)))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == QR_REQUEST_CODE) {
            data?.let {
                handleScannerAppResult(resultCode, it)
            }
        }
    }

    fun handleScannerAppResult(resultCode: Int, data: Intent) {

        runOnUiThread {
            try {
                val scannedText = data.getStringExtra(QR_RESULT_KEY)

                if (resultCode == Activity.RESULT_OK) {
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

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    override fun returnSuccessfulResult() {
        setResult(LOGIN_SUCCEED)
        finish()
    }
}

package com.simprints.id.activities.requestProjectCredentials

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.tools.extensions.scannerAppIntent
import kotlinx.android.synthetic.main.activity_request_project_secret.*

/**
 * Created by fabiotuzza on 18/01/2018.
 */
class RequestProjectCredentialsActivity : AppCompatActivity(), RequestProjectCredentialsContract.View {

    private lateinit var viewPresenter: RequestProjectCredentialsContract.Presenter
    var app: Application? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_project_secret)
        app = (application as Application)
        viewPresenter = RequestProjectCredentialsPresenter(this)
        viewPresenter.start()

        initUI()
    }

    override fun setPresenter(presenter: RequestProjectCredentialsContract.Presenter?) {
        if (presenter == null) return
        viewPresenter = presenter
        viewPresenter.secureDataManager = app?.secureDataManager
    }

    private fun initUI() {
        requestProjectCredentialsButtonScanQr.setOnClickListener { viewPresenter.onScanBarcodeClicked() }
        requestProjectCredentialsButtonEnterSecret.setOnClickListener {
            val projectId = requestProjectCredentialsEditTextId.text.toString()
            val projectSecret = requestProjectCredentialsEditTextSecret.text.toString()
            viewPresenter.onEnterKeyButtonClicked(projectId, projectSecret)
        }
        requestProjectCredentialsButtonManualTyping.setOnClickListener{
            hideButtonToAddProjectDetailsManually()
            showProjectDetails()
        }
    }

    override fun userDidWantToOpenScanQRApp() {
        val intent = packageManager.scannerAppIntent()
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 0)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.zxing.client.android")))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        if (requestCode == 0) {
            val isResultValid = resultCode == Activity.RESULT_OK
            if (isResultValid) {
                val scannedText = data.getStringExtra("SCAN_RESULT")
                var scannedTextComponents = scannedText.split("-")

                val doesScannedTextComponentsTwoParts = scannedTextComponents.size > 1
                if (doesScannedTextComponentsTwoParts) {
                    val projectId = scannedTextComponents[0]
                    val projectSecret = scannedTextComponents[1]

                    this.runOnUiThread({ viewPresenter.onActivityResultForQRScanned(projectId, projectSecret) })
                } else {
                    showErrorForInvalidQRCode()
                }
            }
        }
    }

    fun showErrorForInvalidQRCode() {
        Toast.makeText(this, getString(R.string.requestProjectCredentials_invalidQrCode), Toast.LENGTH_SHORT).show()
    }

    override fun updateProjectSecretInTextView(projectSecret: String) {
        requestProjectCredentialsEditTextSecret.setText(projectSecret)
    }

    override fun updateProjectIdInTextView(projectId: String) {
        requestProjectCredentialsEditTextId.setText(projectId)
    }

    override fun showProjectDetails() {
        requestProjectCredentialsLayoutProjectDetails.visibility = LinearLayout.VISIBLE
    }

    override fun hideButtonToAddProjectDetailsManually() {
        requestProjectCredentialsButtonManualTyping.visibility = LinearLayout.GONE
    }

    override fun showErrorForInvalidProjectCredentials() {
        Toast.makeText(this, getString(R.string.requestProjectCredentials_invalidCredentials), Toast.LENGTH_SHORT).show()
    }

    override fun dismissRequestProjectSecretActivity() {
        finish()
    }
}

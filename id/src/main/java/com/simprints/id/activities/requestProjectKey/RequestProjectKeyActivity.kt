package com.simprints.id.activities.requestProjectKey

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.tools.extensions.scannerAppIntent
import kotlinx.android.synthetic.main.activity_request_project_key.*

/**
 * Created by fabiotuzza on 18/01/2018.
 */
class RequestProjectKeyActivity : AppCompatActivity(), RequestProjectKeyContract.View {

    private lateinit var viewPresenter: RequestProjectKeyContract.Presenter
    var app: Application? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_project_key)
        app = (application as Application)
        viewPresenter = RequestProjectKeyPresenter(this)
        viewPresenter.start()

        initUI()
    }

    override fun setPresenter(presenter: RequestProjectKeyContract.Presenter?) {
        if (presenter == null) return
        viewPresenter = presenter
        viewPresenter.secureDataManager = app?.secureDataManager
    }

    private fun initUI() {
        requestProjectKeyButtonScanQr.setOnClickListener { viewPresenter.onScanBarcodeClicked() }
        requestProjectKeyButtonEnterKey.setOnClickListener { viewPresenter.onEnterKeyButtonClicked(requestProjectKeyEditTextProjectKey.text.toString()) }
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
                val contents = data.getStringExtra("SCAN_RESULT")
                this.runOnUiThread({ viewPresenter.onActivityResultForQRScanned(contents) })
            }
        }
    }

    override fun updateProjectKeyInTextView(projectKey: String) {
        requestProjectKeyEditTextProjectKey.setText(projectKey)
    }

    override fun showErrorForInvalidKey() {
        Toast.makeText(this, getString(R.string.requestProjectKey_invalidKey), Toast.LENGTH_SHORT).show()
    }

    override fun dismissRequestProjectKeyActivity() {
        finish()
    }
}

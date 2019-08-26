package com.simprints.fingerprint.activities.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FingerprintToDomainRequest
import com.simprints.fingerprint.di.KoinInjector.loadFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.unloadFingerprintKoinModules
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException
import com.simprints.fingerprint.tools.extensions.logActivityCreated
import com.simprints.fingerprint.tools.extensions.logActivityDestroyed
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import org.koin.android.viewmodel.ext.android.viewModel

class OrchestratorActivity : AppCompatActivity() {

    private val viewModel: OrchestratorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFingerprintKoinModules()

        logActivityCreated()

        val iFingerprintRequest: IFingerprintRequest = this.intent.extras?.getParcelable(IFingerprintRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForFingerprintException("No IFingerprintRequest found for OrchestratorActivity")
        val fingerprintRequest = FingerprintToDomainRequest.fromFingerprintToDomainRequest(iFingerprintRequest)

        viewModel.finishedResult.observe(this, Observer {
            setResult(it.resultCode, it.resultData)
            finish()
        })

        viewModel.nextActivityCall.observe(this, Observer {
            startActivityForResult(it.createIntent(this), it.requestCode)
        })

        viewModel.start(fingerprintRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.handleActivityResult(OrchestratorViewModel.ActivityResult(resultCode, data))
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadFingerprintKoinModules()
        logActivityDestroyed()
    }
}

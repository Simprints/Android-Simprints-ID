package com.simprints.fingerprint.activities.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FingerprintToDomainRequest
import com.simprints.fingerprint.di.koinModule
import com.simprints.fingerprint.exceptions.unexpected.InvalidRequestForFingerprintException
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class OrchestratorActivity : AppCompatActivity() {

    private val viewModel: OrchestratorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(koinModule)

        val iFingerprintRequest: IFingerprintRequest = this.intent.extras?.getParcelable(IFingerprintRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForFingerprintException()
        viewModel.fingerprintRequest = FingerprintToDomainRequest.fromFingerprintToDomainRequest(iFingerprintRequest)

        viewModel.finishedResult.observe(this, Observer {
            setResult(it.resultCode, it.resultData)
            finish()
        })

        viewModel.nextActivity.observe(this, Observer {
            startActivityForResult(it.toIntent(this), it.resultCode)
        })

        viewModel.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.finishedResult.postValue(FinishedResult(resultCode, data))
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(koinModule)
    }
}

package com.simprints.fingerprint.activities.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FingerprintToDomainRequest
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException
import com.simprints.fingerprint.orchestrator.state.OrchestratorState
import com.simprints.fingerprint.tools.extensions.logActivityCreated
import com.simprints.fingerprint.tools.extensions.logActivityDestroyed
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import org.koin.android.viewmodel.ext.android.viewModel

class OrchestratorActivity : AppCompatActivity() {

    private val viewModel: OrchestratorViewModel by viewModel()

    private val nextActivityCallObserver = Observer<OrchestratorViewModel.ActivityCall> {
        startActivityForResult(it.createIntent(this), it.requestCode)
    }

    private val finishedResultObserver = Observer<OrchestratorViewModel.ActivityResult> {
        setResult(it.resultCode, it.resultData)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acquireFingerprintKoinModules()

        logActivityCreated()

        val iFingerprintRequest: IFingerprintRequest = this.intent.extras?.getParcelable(IFingerprintRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForFingerprintException("No IFingerprintRequest found for OrchestratorActivity")
        val fingerprintRequest = FingerprintToDomainRequest.fromFingerprintToDomainRequest(iFingerprintRequest)

        viewModel.start(fingerprintRequest)
    }

    override fun onResume() {
        super.onResume()
        viewModel.nextActivityCall.observe(this, nextActivityCallObserver)
        viewModel.finishedResult.observe(this, finishedResultObserver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.handleActivityResult(OrchestratorViewModel.ActivityResult(resultCode, data))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.getParcelable<OrchestratorState>(RESTORE_STATE_KEY)?.let {
            viewModel.restoreState(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(RESTORE_STATE_KEY, viewModel.getState())
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseFingerprintKoinModules()
        logActivityDestroyed()
    }

    companion object {
        const val RESTORE_STATE_KEY = "state"
    }
}

package com.simprints.fingerprint.activities.orchestrator

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.fingerprint.connect.screens.ShowConnectWrapper
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FingerprintToDomainRequest
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.state.OrchestratorState
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrchestratorActivity : FingerprintActivity() {

    private val viewModel: OrchestratorViewModel by viewModels()

    private val showConnect = registerForActivityResult(ShowConnectWrapper()) { result ->
        when (result) {
            is FingerprintConnectResult -> {
                if (result.isSuccess) {
                    viewModel.handleActivityResult(OrchestratorViewModel.ActivityResult(
                        ResultCode.OK.value,
                        Intent().putExtra(ConnectScannerTaskResult.BUNDLE_KEY, ConnectScannerTaskResult())
                    ))
                } else {
                    viewModel.handleActivityResult(OrchestratorViewModel.ActivityResult(
                        ResultCode.CANCELLED.value,
                        Intent().putExtra(ConnectScannerTaskResult.BUNDLE_KEY, AlertTaskResult(AlertError.UNEXPECTED_ERROR))
                    ))
                }
            }

            is ExitFormResult -> {
                val option = result.submittedOption()!!
                viewModel.handleActivityResult(OrchestratorViewModel.ActivityResult(
                    ResultCode.REFUSED.value,
                    Intent().putExtra(RefusalTaskResult.BUNDLE_KEY, RefusalTaskResult(
                        RefusalTaskResult.Action.SUBMIT,
                        RefusalTaskResult.Answer(RefusalFormReason.fromExitFormOption(option), result.reason.orEmpty())
                    ))
                ))
            }
        }
    }

    private val nextActivityCallObserver = Observer<FingerprintTask> { task ->
        when (task) {
            is FingerprintTask.ConnectScanner -> showConnect.launch(false)
            is FingerprintTask.CollectFingerprints -> {
                startActivityForResult(
                    Intent(this, task.targetActivity).putExtra(task.requestBundleKey, task.createTaskRequest()),
                    task.requestCode.value
                )
            }
        }
    }

    private val finishedResultObserver = Observer<OrchestratorViewModel.ActivityResult> {
        setResult(it.resultCode, it.resultData)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val iFingerprintRequest: IFingerprintRequest = this.intent.extras?.getParcelable(IFingerprintRequest.BUNDLE_KEY)
                ?: throw InvalidRequestForFingerprintException("No IFingerprintRequest found for OrchestratorActivity")
            val fingerprintRequest = FingerprintToDomainRequest.fromFingerprintToDomainRequest(iFingerprintRequest)

            viewModel.start(fingerprintRequest)
        } else {
            savedInstanceState.getParcelable<OrchestratorState>(RESTORE_STATE_KEY)?.let {
                viewModel.restoreState(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.nextActivityCall.observe(this, nextActivityCallObserver)
        viewModel.finishedResult.observe(this, finishedResultObserver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.COLLECT.value) {
            viewModel.handleActivityResult(OrchestratorViewModel.ActivityResult(resultCode, data))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(RESTORE_STATE_KEY, viewModel.getState())
    }

    companion object {
        const val RESTORE_STATE_KEY = "state"
    }
}

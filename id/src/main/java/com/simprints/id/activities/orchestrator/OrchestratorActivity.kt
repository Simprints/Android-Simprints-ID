package com.simprints.id.activities.orchestrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.orchestrator.di.OrchestratorComponentInjector
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorReason.Companion.fromDomainAlertTypeToAppErrorType
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

open class OrchestratorActivity : AppCompatActivity(), OrchestratorContract.View {

    @Inject
    override lateinit var viewPresenter: OrchestratorContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        OrchestratorComponentInjector.inject(this)
        super.onCreate(savedInstanceState)

        val appRequest = this.intent.extras?.getParcelable<AppRequest>(AppRequest.BUNDLE_KEY)
            ?: throw InvalidAppRequest()

        viewPresenter.appRequest = appRequest
        viewPresenter.start()
    }

    override fun startNextActivity(requestCode:Int, intent: Intent) {
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val potentialAppAlertScreenResult = data?.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)
        potentialAppAlertScreenResult?.let {
            setResultAndFinish(AppErrorResponse(fromDomainAlertTypeToAppErrorType(it.alertType)))
        } ?: viewPresenter.handleResult(requestCode, resultCode, data)
    }

    override fun setCancelResultAndFinish() {
        setResult(Activity.RESULT_CANCELED)
        finishOrchestratorAct()
    }

    override fun setResultAndFinish(response: AppResponse) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IAppResponse.BUNDLE_KEY, viewPresenter.fromDomainToAppResponse(response))
        })

        finishOrchestratorAct()
    }

    override fun finishOrchestratorAct() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        OrchestratorComponentInjector.component = null
    }
}

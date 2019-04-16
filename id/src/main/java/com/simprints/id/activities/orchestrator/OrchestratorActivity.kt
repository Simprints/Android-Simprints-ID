package com.simprints.id.activities.orchestrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.moduleapi.app.responses.IAppResponse

open class OrchestratorActivity : AppCompatActivity(), OrchestratorContract.View {

    override lateinit var viewPresenter: OrchestratorContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appRequest = this.intent.extras?.getParcelable<AppRequest>(AppRequest.BUNDLE_KEY)
            ?: throw IllegalArgumentException("No AppRequest in the bundle") //STOPSHIP

        startPresenter(appRequest)
    }

    internal open fun startPresenter(appRequest: AppRequest) {
        viewPresenter = OrchestratorPresenter(this, appRequest, (application as Application).component)
        viewPresenter.start()
    }

    override fun startNextActivity(requestCode:Int, intent: Intent) {
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewPresenter.handleResult(requestCode, resultCode, data)
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
}

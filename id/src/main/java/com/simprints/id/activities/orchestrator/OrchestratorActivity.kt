package com.simprints.id.activities.orchestrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

class OrchestratorActivity : AppCompatActivity(), OrchestratorContract.View {

    @Inject lateinit var preferences: PreferencesManager

    override lateinit var viewPresenter: OrchestratorContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appRequest = this.intent.extras?.getParcelable<AppRequest>(AppRequest.BUNDLE_KEY)
            ?: throw IllegalArgumentException("No AppRequest in the bundle") //STOPSHIP

        viewPresenter = OrchestratorPresenter(this, appRequest, (application as Application).component)
        viewPresenter.start()
    }

    override fun startActivity(requestCode:Int, intent: Intent) {
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
        setResult(Activity.RESULT_OK, Intent().apply { putExtra(IAppResponse.BUNDLE_KEY, response) })
        finishOrchestratorAct()
    }

    override fun finishOrchestratorAct() {
        finish()
    }
}

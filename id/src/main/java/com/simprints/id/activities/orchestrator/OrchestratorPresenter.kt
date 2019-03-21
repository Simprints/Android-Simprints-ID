package com.simprints.id.activities.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.OrchestratorManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class OrchestratorPresenter(val view: OrchestratorContract.View,
                            val appRequest: AppRequest,
                            component: AppComponent) : OrchestratorContract.Presenter {

    @Inject lateinit var orchestratorManager: OrchestratorManager
    @Inject lateinit var sessionEventsMananager: SessionEventsManager

    init {
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun start() {
        orchestratorManager.flow
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    view.startActivity(it.requestCode, it.intent)
                },
                onComplete = {
                    val finalAppResponse = orchestratorManager.finalAppResponse
                    finalAppResponse?.let {
                        view.setResultAndFinish(it)
                    } ?: view.setCancelResultAndFinish()
                },
                onError = {
                    view.setCancelResultAndFinish()
                    Timber.d(it.message)
                })

        orchestratorManager.startFlow(appRequest, sessionEventsMananager.getCurrentSession().blockingGet().id) //StopShip: Avoid blocking get
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        orchestratorManager.notifyResult(requestCode, resultCode, data)
    }
}

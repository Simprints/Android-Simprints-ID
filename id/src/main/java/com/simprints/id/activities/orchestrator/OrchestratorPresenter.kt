package com.simprints.id.activities.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.OrchestratorManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class OrchestratorPresenter(val view: OrchestratorContract.View,
                            val appRequest: AppRequest,
                            component: AppComponent) : OrchestratorContract.Presenter {

    @Inject lateinit var orchestratorManager: OrchestratorManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager

    init {
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun start() {
        orchestratorManager.startFlow(appRequest, sessionEventsManager.getCurrentSession().blockingGet().id) //StopShip: Avoid blocking get            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    view.startActivity(it.requestCode, it.intent)
                },
                onError = {
                    it.printStackTrace()
                    view.setCancelResultAndFinish()
                    Timber.d(it.message)
                })

        orchestratorManager.getAppResponse()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    view.setResultAndFinish(it)
                },
                onError = {
                    it.printStackTrace()
                    view.setCancelResultAndFinish()
                    Timber.d(it.message)
                })
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        orchestratorManager.onModalStepRequestDone(requestCode, resultCode, data)
    }
}

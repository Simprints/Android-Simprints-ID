package com.simprints.id.activities.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.activities.orchestrator.di.OrchestratorComponentInjector
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.moduleapi.app.DomainToAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.moduleapi.app.responses.IAppResponse
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class OrchestratorPresenter: OrchestratorContract.Presenter {

    @Inject lateinit var orchestratorManager: OrchestratorManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper
    @Inject lateinit var timeHelper: TimeHelper

    @Inject
    lateinit var view: OrchestratorContract.View

    override lateinit var appRequest: AppRequest

    init {
        OrchestratorComponentInjector.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun start() {
        subscribeForModalitiesResponses()

        syncSchedulerHelper.scheduleBackgroundSyncs()
        syncSchedulerHelper.startDownSyncOnLaunchIfPossible()
    }

    @SuppressLint("CheckResult")
    internal fun subscribeForModalitiesResponses() =
        getSessionId().flatMapObservable {
            orchestratorManager.startFlow(appRequest, it).also {
                subscribeForFinalAppResponse()
            }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onNext = {
                handleNextModalityRequest(it)
            },
            onError = {
                handleErrorInTheModalitiesFlow(it)
            })

    @SuppressLint("CheckResult")
    internal fun subscribeForFinalAppResponse() =
        orchestratorManager.getAppResponse()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    handleAppResponse(it)
                },
                onError = {
                    handleErrorInTheModalitiesFlow(it)
                })

    private fun handleAppResponse(appResponse: AppResponse) {
        addCallbackEventInSessions(appResponse)
        view.setResultAndFinish(appResponse)
    }

    @SuppressLint("CheckResult")
    internal fun getSessionId(): Single<String> =
        sessionEventsManager.getCurrentSession().map { it.id }

    private fun handleNextModalityRequest(modalityRequest: ModalityStepRequest) =
        view.startNextActivity(modalityRequest.requestCode, modalityRequest.intent)

    private fun handleErrorInTheModalitiesFlow(it: Throwable) {
        it.printStackTrace()
        view.setCancelResultAndFinish()
        Timber.d(it.message)
    }

    internal fun addCallbackEventInSessions(appResponse: AppResponse) {
        when(appResponse) {
            is AppEnrolResponse -> sessionEventsManager
                .addSessionEvent(buildEnrolmentCallbackEvent(appResponse))
            is AppIdentifyResponse -> sessionEventsManager
                .addSessionEvent(buildIdentificationCallbackEvent(appResponse))
            is AppVerifyResponse -> sessionEventsManager
                .addSessionEvent(buildVerificationCallbackEvent(appResponse))
            is AppRefusalFormResponse -> sessionEventsManager
                .addSessionEvent(buildRefusalCallbackEvent(appResponse))
        }
    }

    internal fun buildEnrolmentCallbackEvent(appResponse: AppEnrolResponse) =
        CallbackEvent(timeHelper.now(), EnrolmentCallback(appResponse.guid))

    internal fun buildIdentificationCallbackEvent(appResponse: AppIdentifyResponse) =
        CallbackEvent(timeHelper.now(), buildIdentificationCallback(appResponse))

    private fun buildIdentificationCallback(appIdentifyResponse: AppIdentifyResponse) =
        IdentificationCallback(appIdentifyResponse.sessionId,
            appIdentifyResponse.identifications.map {
                CallbackComparisonScore(it.guidFound, it.confidence, it.tier)
            })

    internal fun buildVerificationCallbackEvent(appVerifyResponse: AppVerifyResponse) =
        CallbackEvent(timeHelper.now(),
            VerificationCallback(appVerifyResponse.matchingResult.let {
                CallbackComparisonScore(it.guidFound, it.confidence, it.tier)
            }))

    internal fun buildRefusalCallbackEvent(appRefusalResponse: AppRefusalFormResponse) =
        CallbackEvent(timeHelper.now(), RefusalCallback(getAppRefusalResponseReasonOrEmpty(appRefusalResponse),
            appRefusalResponse.answer.optionalText))

    private fun getAppRefusalResponseReasonOrEmpty(appRefusalResponse: AppRefusalFormResponse) =
        appRefusalResponse.answer.reason?.name ?: ""

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        orchestratorManager.onModalStepRequestDone(requestCode, resultCode, data)
    }

    override fun fromDomainToAppResponse(response: AppResponse?): IAppResponse? =
        DomainToAppResponse.fromDomainToAppResponse(response)
}

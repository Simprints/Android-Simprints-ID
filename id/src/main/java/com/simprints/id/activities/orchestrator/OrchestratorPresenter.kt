package com.simprints.id.activities.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.activities.orchestrator.di.OrchestratorComponentInjector
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.*
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.domain.moduleapi.app.DomainToAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.moduleapi.app.responses.IAppResponse
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class OrchestratorPresenter : OrchestratorContract.Presenter {

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
            .map {
                addCallbackEventInSessions(it).blockingGet()
                it
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    view.setResultAndFinish(it)
                },
                onError = {
                    handleErrorInTheModalitiesFlow(it)
                })

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

    internal fun addCallbackEventInSessions(appResponse: AppResponse) =

        sessionEventsManager.updateSession { session ->
            val relativeStartTime = session.timeRelativeToStartTime(timeHelper.now())

            when (appResponse) {
                is AppEnrolResponse -> buildEnrolmentCallbackEvent(appResponse, relativeStartTime)
                is AppIdentifyResponse -> buildIdentificationCallbackEvent(appResponse, relativeStartTime)
                is AppVerifyResponse -> buildVerificationCallbackEvent(appResponse, relativeStartTime)
                is AppRefusalFormResponse -> buildRefusalCallbackEvent(appResponse, relativeStartTime)
                else -> null
            }?.let {
                session.addEvent(it)
            }
        }

    internal fun buildEnrolmentCallbackEvent(appResponse: AppEnrolResponse, relativeStartTime: Long) =
        EnrolmentCallbackEvent(relativeStartTime, appResponse.guid)

    internal fun buildIdentificationCallbackEvent(appResponse: AppIdentifyResponse, relativeStartTime: Long) =
        with(appResponse) {
            IdentificationCallbackEvent(
                relativeStartTime,
                sessionId,
                identifications.map {
                    CallbackComparisonScore(it.guidFound, it.confidence, it.tier)
                })
        }

    internal fun buildVerificationCallbackEvent(appVerifyResponse: AppVerifyResponse, relativeStartTime: Long) =
        with(appVerifyResponse.matchingResult) {
            VerificationCallbackEvent(relativeStartTime,
                CallbackComparisonScore(guidFound, confidence, tier))
        }

    internal fun buildRefusalCallbackEvent(appRefusalResponse: AppRefusalFormResponse, relativeStartTime: Long) =
        with(appRefusalResponse) {
            RefusalCallbackEvent(
                relativeStartTime,
                answer.reason?.name ?: "",
                answer.optionalText)
        }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        orchestratorManager.onModalStepRequestDone(requestCode, resultCode, data)
    }

    override fun fromDomainToAppResponse(response: AppResponse?): IAppResponse? =
        DomainToAppResponse.fromDomainToAppResponse(response)
}

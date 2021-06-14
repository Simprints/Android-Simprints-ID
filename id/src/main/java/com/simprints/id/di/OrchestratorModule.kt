package com.simprints.id.di

import android.content.Context
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelper
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelperImpl
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.orchestrator.*
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.modality.*
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactoryImpl
import com.simprints.id.orchestrator.responsebuilders.adjudication.EnrolResponseAdjudicationHelper
import com.simprints.id.orchestrator.responsebuilders.adjudication.EnrolResponseAdjudicationHelperImpl
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.tools.extensions.deviceId
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class OrchestratorModule {

    @Provides
    fun provideFaceRequestFactory(): FaceRequestFactory = FaceRequestFactoryImpl()

    @Provides
    fun provideFingerprintRequestFactory(): FingerprintRequestFactory =
        FingerprintRequestFactoryImpl()

    @Provides
    fun provideFaceStepProcessor(
        faceRequestFactory: FaceRequestFactory,
        preferenceManager: IdPreferencesManager
    ): FaceStepProcessor =
        FaceStepProcessorImpl(faceRequestFactory, preferenceManager)

    @Provides
    fun provideFingerprintStepProcessor(
        fingerprintRequestFactory: FingerprintRequestFactory,
        preferenceManager: IdPreferencesManager
    ): FingerprintStepProcessor =
        FingerprintStepProcessorImpl(fingerprintRequestFactory, preferenceManager)

    @Provides
    fun provideCoreStepProcessor(): CoreStepProcessor = CoreStepProcessorImpl()

    // ModalFlow [ConfirmIdentity, Enrol, Identify, Verify, EnrolLastBiometrics]
    @Provides
    @Named("ModalityConfirmationFlow")
    fun provideModalityFlowConfirmIdentity(
        coreStepProcessor: CoreStepProcessor
    ): ModalityFlow =
        ModalityFlowConfirmIdentity(
            coreStepProcessor
        )

    @Provides
    @Named("ModalityEnrolLastBiometricsFlow")
    fun provideModalityEnrolLastBiometricsFlow(
        coreStepProcessor: CoreStepProcessor,
        hotCache: HotCache
    ): ModalityFlow =
        ModalityFlowEnrolLastBiometrics(
            coreStepProcessor,
            hotCache
        )

    @Provides
    @Named("ModalityFlowEnrol")
    fun provideModalityFlow(
        fingerprintStepProcessor: FingerprintStepProcessor,
        faceStepProcessor: FaceStepProcessor,
        coreStepProcessor: CoreStepProcessor,
        timeHelper: TimeHelper,
        eventRepository: EventRepository,
        preferenceManager: IdPreferencesManager,
        loginInfoManager: LoginInfoManager,
        ctx: Context
    ): ModalityFlow =
        ModalityFlowEnrolImpl(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            timeHelper,
            eventRepository,
            preferenceManager.consentRequired,
            preferenceManager.locationPermissionRequired,
            preferenceManager.modalities,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            ctx.deviceId,
            preferenceManager.isEnrolmentPlus,
            preferenceManager.matchGroup
        )

    @Provides
    @Named("ModalityFlowVerify")
    fun provideModalityFlowVerify(
        fingerprintStepProcessor: FingerprintStepProcessor,
        faceStepProcessor: FaceStepProcessor,
        coreStepProcessor: CoreStepProcessor,
        timeHelper: TimeHelper,
        eventRepository: EventRepository,
        preferenceManager: IdPreferencesManager,
        loginInfoManager: LoginInfoManager,
        ctx: Context
    ): ModalityFlow =
        ModalityFlowVerifyImpl(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            timeHelper,
            eventRepository,
            preferenceManager.consentRequired,
            preferenceManager.locationPermissionRequired,
            preferenceManager.modalities,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            ctx.deviceId
        )

    @Provides
    @Named("ModalityFlowIdentify")
    fun provideModalityFlowIdentify(
        fingerprintStepProcessor: FingerprintStepProcessor,
        faceStepProcessor: FaceStepProcessor,
        coreStepProcessor: CoreStepProcessor,
        timeHelper: TimeHelper,
        prefs: IdPreferencesManager,
        eventRepository: EventRepository,
        loginInfoManager: LoginInfoManager,
        ctx: Context
    ): ModalityFlow =
        ModalityFlowIdentifyImpl(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            prefs.matchGroup,
            timeHelper,
            eventRepository,
            prefs.consentRequired,
            prefs.locationPermissionRequired,
            prefs.modalities,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            ctx.deviceId
        )

    // Orchestration
    @Provides
    fun provideModalityFlowFactory(
        @Named("ModalityFlowEnrol") enrolFlow: ModalityFlow,
        @Named("ModalityFlowVerify") verifyFlow: ModalityFlow,
        @Named("ModalityFlowIdentify") identifyFlow: ModalityFlow,
        @Named("ModalityConfirmationFlow") confirmationIdentityFlow: ModalityFlow,
        @Named("ModalityEnrolLastBiometricsFlow") enrolLastBiometricsFlow: ModalityFlow
    ): ModalityFlowFactory =
        ModalityFlowFactoryImpl(enrolFlow, verifyFlow, identifyFlow, confirmationIdentityFlow, enrolLastBiometricsFlow)

    @Provides
    fun provideOrchestratorManager(orchestratorManagerImpl: OrchestratorManagerImpl): OrchestratorManager {
        return orchestratorManagerImpl
    }

    @Provides
    @OrchestratorScope // Since OrchestratorManagerImpl is also a FlowProvider, it needs to be a Singleton
    fun provideOrchestratorManagerImpl(
        modalityFlowFactory: ModalityFlowFactory,
        appResponseFactory: AppResponseFactory,
        hotCache: HotCache,
        dashboardDailyActivityRepository: DashboardDailyActivityRepository,
        personCreationEventHelper: PersonCreationEventHelper
    ): OrchestratorManagerImpl = OrchestratorManagerImpl(
        modalityFlowFactory,
        appResponseFactory,
        hotCache,
        dashboardDailyActivityRepository,
        personCreationEventHelper
    )

    @Provides
    fun provideOrchestratorEventsHelper(
        eventRepository: EventRepository,
        timeHelper: TimeHelper
    ): OrchestratorEventsHelper =
        OrchestratorEventsHelperImpl(eventRepository, timeHelper)

    @Provides
    fun provideAppResponseBuilderFactory(
        enrolmentHelper: EnrolmentHelper,
        timeHelper: TimeHelper,
        preferenceManager: IdPreferencesManager,
        enrolResponseAdjudicationHelper: EnrolResponseAdjudicationHelper
    ): AppResponseFactory = AppResponseFactoryImpl(
        enrolmentHelper,
        timeHelper,
        preferenceManager.isEnrolmentPlus,
        preferenceManager.fingerprintConfidenceThresholds,
        preferenceManager.faceConfidenceThresholds,
        preferenceManager.returnIdCount,
        enrolResponseAdjudicationHelper
    )

    @Provides
    fun provideFlowManager(
        orchestratorManagerImpl: OrchestratorManagerImpl
    ): FlowProvider = orchestratorManagerImpl

    @Provides
    fun provideEnrolAdjudicationActionHelper(prefs: IdPreferencesManager): EnrolResponseAdjudicationHelper =
        EnrolResponseAdjudicationHelperImpl(prefs.fingerprintConfidenceThresholds, prefs.faceConfidenceThresholds)

}

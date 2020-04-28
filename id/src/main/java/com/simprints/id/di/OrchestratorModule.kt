package com.simprints.id.di

import android.content.SharedPreferences
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelper
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelperImpl
import com.simprints.id.activities.orchestrator.OrchestratorViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.orchestrator.*
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.StepEncoder
import com.simprints.id.orchestrator.cache.StepEncoderImpl
import com.simprints.id.orchestrator.modality.*
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactoryImpl
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.tools.TimeHelper
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
    fun provideFaceStepProcessor(faceRequestFactory: FaceRequestFactory): FaceStepProcessor =
        FaceStepProcessorImpl(faceRequestFactory)

    @Provides
    fun provideFingerprintStepProcessor(
        fingerprintRequestFactory: FingerprintRequestFactory,
        preferenceManager: PreferencesManager
    ): FingerprintStepProcessor =
        FingerprintStepProcessorImpl(fingerprintRequestFactory, preferenceManager)

    @Provides
    fun provideCoreStepProcessor(): CoreStepProcessor = CoreStepProcessorImpl()

    // ModalFlow [ConfirmIdentity, Enrol, Identify, Verify]
    @Provides
    @Named("ModalityConfirmationFlow")
    fun provideModalityFlowConfirmIdentity(
        coreStepProcessor: CoreStepProcessor
    ): ModalityFlow =
        ModalityFlowConfirmIdentity(
            coreStepProcessor
        )

    @Provides
    @Named("ModalityFlowEnrol")
    fun provideModalityFlow(
        fingerprintStepProcessor: FingerprintStepProcessor,
        faceStepProcessor: FaceStepProcessor,
        coreStepProcessor: CoreStepProcessor,
        timeHelper: TimeHelper,
        sessionRepository: SessionRepository,
        preferenceManager: PreferencesManager
    ): ModalityFlow =
        ModalityFlowEnrolImpl(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            timeHelper,
            sessionRepository,
            preferenceManager.consentRequired
        )

    @Provides
    @Named("ModalityFlowVerify")
    fun provideModalityFlowVerify(
        fingerprintStepProcessor: FingerprintStepProcessor,
        faceStepProcessor: FaceStepProcessor,
        coreStepProcessor: CoreStepProcessor,
        timeHelper: TimeHelper,
        sessionRepository: SessionRepository,
        preferenceManager: PreferencesManager
    ): ModalityFlow =
        ModalityFlowVerifyImpl(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            timeHelper,
            sessionRepository,
            preferenceManager.consentRequired
        )

    @Provides
    @Named("ModalityFlowIdentify")
    fun provideModalityFlowIdentify(
        fingerprintStepProcessor: FingerprintStepProcessor,
        faceStepProcessor: FaceStepProcessor,
        coreStepProcessor: CoreStepProcessor,
        timeHelper: TimeHelper,
        prefs: PreferencesManager,
        sessionRepository: SessionRepository
    ): ModalityFlow =
        ModalityFlowIdentifyImpl(
            fingerprintStepProcessor, faceStepProcessor,
            coreStepProcessor, prefs.matchGroup, timeHelper, sessionRepository, prefs.consentRequired
        )

    // Orchestration
    @Provides
    fun provideModalityFlowFactory(
        @Named("ModalityFlowEnrol") enrolFlow: ModalityFlow,
        @Named("ModalityFlowVerify") verifyFlow: ModalityFlow,
        @Named("ModalityFlowIdentify") identifyFlow: ModalityFlow,
        @Named("ModalityConfirmationFlow") confirmationIdentityFlow: ModalityFlow
    ): ModalityFlowFactory =
        ModalityFlowFactoryImpl(enrolFlow, verifyFlow, identifyFlow, confirmationIdentityFlow)

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
        dashboardDailyActivityRepository: DashboardDailyActivityRepository
    ): OrchestratorManagerImpl = OrchestratorManagerImpl(
        modalityFlowFactory,
        appResponseFactory,
        hotCache,
        dashboardDailyActivityRepository
    )

    @Provides
    fun provideOrchestratorEventsHelper(
        sessionRepository: SessionRepository,
        timeHelper: TimeHelper
    ): OrchestratorEventsHelper =
        OrchestratorEventsHelperImpl(sessionRepository, timeHelper)

    @Provides
    fun provideOrchestratorViewModelFactory(
        orchestratorManager: OrchestratorManager,
        orchestratorEventsHelper: OrchestratorEventsHelper,
        preferenceManager: PreferencesManager,
        sessionRepository: SessionRepository,
        crashReportManager: CrashReportManager
    ): OrchestratorViewModelFactory {
        return OrchestratorViewModelFactory(
            orchestratorManager,
            orchestratorEventsHelper,
            preferenceManager.modalities,
            sessionRepository,
            DomainToModuleApiAppResponse,
            crashReportManager
        )
    }

    @Provides
    fun provideHotCache(
        @Named("EncryptedSharedPreferences") sharedPrefs: SharedPreferences,
        stepEncoder: StepEncoder
    ): HotCache = HotCacheImpl(sharedPrefs, stepEncoder)

    @Provides
    fun provideStepEncoder(): StepEncoder = StepEncoderImpl()

    @Provides
    open fun provideAppResponseBuilderFactory(
        enrolmentHelper: EnrolmentHelper,
        timeHelper: TimeHelper
    ): AppResponseFactory = AppResponseFactoryImpl(enrolmentHelper, timeHelper)

    @Provides
    fun provideEnrolmentHelper(
        repository: PersonRepository,
        sessionRepository: SessionRepository,
        timeHelper: TimeHelper
    ): EnrolmentHelper = EnrolmentHelperImpl(repository, sessionRepository, timeHelper)

    @Provides
    fun provideFlowManager(
        orchestratorManagerImpl: OrchestratorManagerImpl
    ): FlowProvider = orchestratorManagerImpl

}

package com.simprints.id.di

import android.content.Context
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelper
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelperImpl
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
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
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
        configManager: ConfigManager,
    ): FaceStepProcessor =
        FaceStepProcessorImpl(faceRequestFactory, configManager)

    @Provides
    fun provideFingerprintStepProcessor(
        fingerprintRequestFactory: FingerprintRequestFactory,
        configManager: ConfigManager,
    ): FingerprintStepProcessor =
        FingerprintStepProcessorImpl(fingerprintRequestFactory, configManager)

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
        configManager: ConfigManager,
        loginManager: LoginManager,
        ctx: Context
    ): ModalityFlow =
        ModalityFlowEnrol(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            configManager,
            loginManager,
            ctx.deviceId
        )

    @Provides
    @Named("ModalityFlowVerify")
    fun provideModalityFlowVerify(
        fingerprintStepProcessor: FingerprintStepProcessor,
        faceStepProcessor: FaceStepProcessor,
        coreStepProcessor: CoreStepProcessor,
        configManager: ConfigManager,
        loginManager: LoginManager,
        ctx: Context
    ): ModalityFlow =
        ModalityFlowVerify(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            configManager,
            loginManager,
            ctx.deviceId
        )

    @Provides
    @Named("ModalityFlowIdentify")
    fun provideModalityFlowIdentify(
        fingerprintStepProcessor: FingerprintStepProcessor,
        faceStepProcessor: FaceStepProcessor,
        coreStepProcessor: CoreStepProcessor,
        configManager: ConfigManager,
        loginManager: LoginManager,
        ctx: Context
    ): ModalityFlow =
        ModalityFlowIdentify(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            configManager,
            loginManager,
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
        ModalityFlowFactoryImpl(
            enrolFlow,
            verifyFlow,
            identifyFlow,
            confirmationIdentityFlow,
            enrolLastBiometricsFlow
        )

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
        configManager: ConfigManager,
        enrolResponseAdjudicationHelper: EnrolResponseAdjudicationHelper
    ): AppResponseFactory = AppResponseFactoryImpl(
        enrolmentHelper,
        timeHelper,
        configManager,
        enrolResponseAdjudicationHelper
    )

    @Provides
    fun provideFlowManager(
        orchestratorManagerImpl: OrchestratorManagerImpl
    ): FlowProvider = orchestratorManagerImpl

    @Provides
    fun provideEnrolAdjudicationActionHelper(): EnrolResponseAdjudicationHelper =
        EnrolResponseAdjudicationHelperImpl()

}

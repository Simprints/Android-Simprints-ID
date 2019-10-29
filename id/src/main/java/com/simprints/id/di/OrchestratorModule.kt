package com.simprints.id.di

import android.content.SharedPreferences
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelper
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelperImpl
import com.simprints.id.activities.orchestrator.OrchestratorViewModelFactory
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.orchestrator.*
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.crypto.step.StepEncoder
import com.simprints.id.orchestrator.cache.crypto.step.StepEncoderImpl
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImpl
import com.simprints.id.orchestrator.modality.ModalityFlowIdentifyImpl
import com.simprints.id.orchestrator.modality.ModalityFlowVerifyImpl
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.secure.cryptography.HybridCipher
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class OrchestratorModule {

    @Provides
    fun provideFaceRequestFactory(): FaceRequestFactory = FaceRequestFactoryImpl()

    @Provides
    fun provideFingerprintRequestFactory(): FingerprintRequestFactory = FingerprintRequestFactoryImpl()

    @Provides
    fun provideFaceStepProcessor(faceRequestFactory: FaceRequestFactory): FaceStepProcessor =
        FaceStepProcessorImpl(faceRequestFactory)

    @Provides
    fun provideFingerprintStepProcessor(fingerprintRequestFactory: FingerprintRequestFactory,
                                        preferenceManager: PreferencesManager): FingerprintStepProcessor =
        FingerprintStepProcessorImpl(fingerprintRequestFactory, preferenceManager)

    @Provides
    fun provideCoreStepProcessor(): CoreStepProcessor = CoreStepProcessorImpl()

    // ModalFlow [Enrol, Identify, Verify]
    @Provides
    @Named("ModalityFlowEnrol")
    fun provideModalityFlow(fingerprintStepProcessor: FingerprintStepProcessor,
                            faceStepProcessor: FaceStepProcessor, coreStepProcessor: CoreStepProcessor): ModalityFlow =
        ModalityFlowEnrolImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor)

    @Provides
    @Named("ModalityFlowVerify")
    fun provideModalityFlowVerify(fingerprintStepProcessor: FingerprintStepProcessor,
                                  faceStepProcessor: FaceStepProcessor,
                                  coreStepProcessor: CoreStepProcessor,
                                  prefs: PreferencesManager): ModalityFlow =
        ModalityFlowVerifyImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor, prefs)

    @Provides
    @Named("ModalityFlowIdentify")
    fun provideModalityFlowIdentify(fingerprintStepProcessor: FingerprintStepProcessor,
                                    faceStepProcessor: FaceStepProcessor,
                                    coreStepProcessor: CoreStepProcessor,
                                    prefs: PreferencesManager): ModalityFlow =
        ModalityFlowIdentifyImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor, prefs)

    // Orchestration
    @Provides
    fun provideModalityFlowFactory(@Named("ModalityFlowEnrol") enrolFlow: ModalityFlow,
                                   @Named("ModalityFlowVerify") verifyFlow: ModalityFlow,
                                   @Named("ModalityFlowIdentify") identifyFlow: ModalityFlow): ModalityFlowFactory =
        ModalityFlowFactoryImpl(enrolFlow, verifyFlow, identifyFlow)

    @Provides
    fun provideOrchestratorManager(orchestratorManagerImpl: OrchestratorManagerImpl): OrchestratorManager {
        return orchestratorManagerImpl
    }

    @Provides
    @Singleton // Since OrchestratorManagerImpl is also a FlowManager, it needs to be a Singleton
    fun provideOrchestratorManagerImpl(modalityFlowFactory: ModalityFlowFactory,
                                       appResponseFactory: AppResponseFactory,
                                       hotCache: HotCache): OrchestratorManagerImpl =
        OrchestratorManagerImpl(modalityFlowFactory, appResponseFactory, hotCache)

    @Provides
    fun provideOrchestratorEventsHelper(sessionEventsManager: SessionEventsManager,
                                        timeHelper: TimeHelper): OrchestratorEventsHelper =
        OrchestratorEventsHelperImpl(sessionEventsManager, timeHelper)

    @Provides
    fun provideOrchestratorViewModelFactory(
        orchestratorManager: OrchestratorManager,
        orchestratorEventsHelper: OrchestratorEventsHelper,
        preferenceManager: PreferencesManager,
        sessionEventsManager: SessionEventsManager
    ): OrchestratorViewModelFactory {
        return OrchestratorViewModelFactory(
            orchestratorManager,
            orchestratorEventsHelper,
            preferenceManager.modalities,
            sessionEventsManager,
            DomainToModuleApiAppResponse
        )
    }

    @Provides
    fun provideHotCache(
        preferences: SharedPreferences,
        stepEncoder: StepEncoder
    ): HotCache = HotCacheImpl(preferences, stepEncoder)

    @Provides
    fun provideStepEncoder(
        cipher: HybridCipher
    ): StepEncoder = StepEncoderImpl(cipher)

    @Provides
    fun provideEnrolmentHelper(
        repository: PersonRepository,
        sessionEventsManager: SessionEventsManager,
        timeHelper: TimeHelper
    ): EnrolmentHelper = EnrolmentHelperImpl(repository, sessionEventsManager, timeHelper)

    @Provides
    fun provideFlowManager(
        orchestratorManagerImpl: OrchestratorManagerImpl
    ): FlowManager = orchestratorManagerImpl

}

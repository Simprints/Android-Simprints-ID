package com.simprints.id.di

import com.simprints.id.activities.orchestrator.OrchestratorViewModelFactory
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.face.ModuleApiToDomainFaceResponse
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.ModuleApiToDomainFingerprintResponse
import com.simprints.id.orchestrator.ModalityFlowFactory
import com.simprints.id.orchestrator.ModalityFlowFactoryImpl
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.OrchestratorManagerImpl
import com.simprints.id.orchestrator.builders.AppResponseFactory
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImpl
import com.simprints.id.orchestrator.modality.ModalityFlowIdentifyImpl
import com.simprints.id.orchestrator.modality.ModalityFlowVerifyImpl
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class AndroidModule {

    @Provides
    fun provideFaceRequestFactory(): FaceRequestFactory = FaceRequestFactoryImpl()

    @Provides
    fun provideFingerprintRequestFactory(): FingerprintRequestFactory = FingerprintRequestFactoryImpl()


    @Provides
    fun provideFaceStepProcessor(faceRequestFactory: FaceRequestFactory): FaceStepProcessor =
        FaceStepProcessorImpl(faceRequestFactory, ModuleApiToDomainFaceResponse)

    @Provides
    fun provideFingerprintStepProcessor(fingerprintRequestFactory: FingerprintRequestFactory,
                                        preferenceManager: PreferencesManager): FingerprintStepProcessor =
        FingerprintStepProcessorImpl(fingerprintRequestFactory, ModuleApiToDomainFingerprintResponse, preferenceManager)

    // ModalFlow [Enrol, Identify, Verify]
    @Provides
    @Named("ModalityFlowEnrol")
    fun provideModalityFlow(fingerprintStepProcessor: FingerprintStepProcessor,
                            faceStepProcessor: FaceStepProcessor): ModalityFlow =
        ModalityFlowEnrolImpl(fingerprintStepProcessor, faceStepProcessor)

    @Provides
    @Named("ModalityFlowVerify")
    fun provideModalityFlowVerify(fingerprintStepProcessor: FingerprintStepProcessor,
                                  faceStepProcessor: FaceStepProcessor): ModalityFlow =
        ModalityFlowVerifyImpl(fingerprintStepProcessor, faceStepProcessor)

    @Provides
    @Named("ModalityFlowIdentify")
    fun provideModalityFlowIdentify(fingerprintStepProcessor: FingerprintStepProcessor,
                                    faceStepProcessor: FaceStepProcessor): ModalityFlow =
        ModalityFlowIdentifyImpl(fingerprintStepProcessor, faceStepProcessor)

    // Orchestration
    @Provides
    fun provideModalityFlowFactory(@Named("ModalityFlowEnrol") enrolFlow: ModalityFlow,
                                   @Named("ModalityFlowVerify") verifyFlow: ModalityFlow,
                                   @Named("ModalityFlowIdentify") identifyFlow: ModalityFlow): ModalityFlowFactory =
        ModalityFlowFactoryImpl(enrolFlow, verifyFlow, identifyFlow)

    @Provides
    @Singleton
    fun provideOrchestratorManager(modalityFlowFactory: ModalityFlowFactory,
                                   appResponseFactory: AppResponseFactory): OrchestratorManager =
        OrchestratorManagerImpl(modalityFlowFactory, appResponseFactory)

    @Provides
    fun provideOrchestratorViewModelFactory(orchestratorManager: OrchestratorManager,
                                            preferenceManager: PreferencesManager,
                                            sessionEventsManager: SessionEventsManager,
                                            timeHelper: TimeHelper) =
        OrchestratorViewModelFactory(orchestratorManager, preferenceManager, sessionEventsManager, timeHelper)

}
